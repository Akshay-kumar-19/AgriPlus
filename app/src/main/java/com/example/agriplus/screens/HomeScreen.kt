package com.example.agriplus.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.agriplus.R
import com.example.agriplus.data.*
import com.example.agriplus.utils.ApiClient
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.io.ByteArrayOutputStream

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val manager = remember { RealtimeManager() }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var diseaseInfo by remember { mutableStateOf<DiseaseInfo?>(null) }
    var confidence by remember { mutableStateOf(0.0) }

    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val plantAnim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.plant))
    val scanAnim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan))

    val plantProgress by animateLottieCompositionAsState(plantAnim, iterations = LottieConstants.IterateForever)
    val scanProgress by animateLottieCompositionAsState(scanAnim, iterations = LottieConstants.IterateForever)

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            uri?.let {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            bitmap = it
        }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun startScan() {
        val bmp = bitmap ?: run {
            errorMessage = "Please select an image first"
            return
        }

        isScanning = true
        errorMessage = ""
        diseaseInfo = null
        confidence = 0.0

        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        ApiClient.uploadImage(stream.toByteArray()) { response ->

            isScanning = false

            try {
                val obj = JSONObject(response)
                    .getJSONArray("predictions")
                    .getJSONObject(0)

                val label = obj.getString("class")
                val conf = obj.getDouble("confidence")

                diseaseInfo = DiseaseRepository.getInfo(label)
                confidence = conf

                val base64Image = bitmapToBase64(bmp)

                manager.saveResult(
                    HistoryItem(label, conf, base64Image, System.currentTimeMillis())
                )

            } catch (e: Exception) {
                errorMessage = "Network error or invalid image"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2027), Color(0xFF2C5364))
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text("Welcome 👋", color = Color.LightGray)
                        Text(user?.displayName ?: "User",
                            color = Color.White,
                            fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔥 CENTERED IMAGE / LOTTIE
            Card(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit, // 🔥 CENTER FIX
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LottieAnimation(
                            composition = plantAnim,
                            progress = { plantProgress },
                            modifier = Modifier.size(200.dp) // 🔥 CENTER + SIZE FIX
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()) {
                Text("Upload Image")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(modifier = Modifier.width(5.dp))
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { startScan() },
                enabled = !isScanning,
                modifier = Modifier.fillMaxWidth()) {
                Text("Scan Leaf")
            }

            // 🔥 CENTERED SCAN LOTTIE
            AnimatedVisibility(visible = isScanning) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = scanAnim,
                        progress = { scanProgress },
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ERROR UI
            if (errorMessage.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(Color.Red)) {
                    Column(Modifier.padding(10.dp)) {
                        Text(errorMessage, color = Color.White)

                        Button(onClick = { startScan() }) {
                            Icon(Icons.Default.Refresh, null)
                            Text(" Retry")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔥 RESULT CARD (COLOR FIXED)
            AnimatedVisibility(visible = diseaseInfo != null) {

                val info = diseaseInfo ?: return@AnimatedVisibility
                val isHealthy = info.name.contains("Healthy", true)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHealthy)
                            Color(0xFF1B5E20) // GREEN
                        else
                            Color(0xFFB71C1C) // RED
                    )
                ) {

                    Column(Modifier.padding(12.dp)) {

                        Text(
                            info.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { confidence.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "Confidence ${(confidence * 100).toInt()}%",
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Cause:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(info.cause, color = Color.White)

                        Text("Symptoms:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(info.symptoms, color = Color.White)

                        Text("Treatment:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(info.treatment, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View History")
            }
        }
    }
}