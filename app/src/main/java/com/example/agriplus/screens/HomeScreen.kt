package com.example.agriplus.screens

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
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

    fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    // CAMERA
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
            bmp?.let {
                bitmap = it
                imageUri = null
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraLauncher.launch(null)
            } else {
                errorMessage = "Camera permission denied"
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                bitmap = getBitmapFromUri(it)
            }
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

                if (label.isEmpty()) {
                    errorMessage = "Invalid image"
                    return@uploadImage
                }

                diseaseInfo = DiseaseRepository.getInfo(label)
                confidence = conf

                manager.saveResult(
                    HistoryItem(label, conf, bitmapToBase64(bmp), System.currentTimeMillis())
                )

            } catch (e: Exception) {
                errorMessage = "Network error"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF2C5364))))
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {

            // HEADER
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text("Welcome 👋", color = Color.LightGray)
                        Text(
                            user?.displayName ?: "User",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // IMAGE / LOTTIE
            Card(Modifier.fillMaxWidth().height(260.dp), shape = RoundedCornerShape(20.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                    when {
                        bitmap != null -> {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        imageUri != null -> {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            LottieAnimation(plantAnim, { plantProgress }, Modifier.size(200.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button({ galleryLauncher.launch("image/*") }, Modifier.fillMaxWidth()) {
                Text("Upload Image")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button({
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }, Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(5.dp))
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                { startScan() },
                Modifier.fillMaxWidth(),
                enabled = !isScanning
            ) {
                Text("Scan Leaf")
            }

            AnimatedVisibility(isScanning) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    LottieAnimation(scanAnim, { scanProgress }, Modifier.size(120.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ERROR
            if (errorMessage.isNotEmpty()) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.Red)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(errorMessage, color = Color.White)
                        Spacer(Modifier.height(6.dp))
                        Button({ startScan() }, Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // RESULT (SAFE FIX)
            AnimatedVisibility(visible = diseaseInfo != null) {

                val info = diseaseInfo ?: return@AnimatedVisibility
                val isHealthy = info.name.contains("healthy", true)

                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        if (isHealthy) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                ) {

                    Column(Modifier.padding(12.dp)) {

                        Text(info.name, color = Color.White, fontWeight = FontWeight.Bold)

                        LinearProgressIndicator(
                            progress = { confidence.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Confidence ${(confidence * 100).toInt()}%", color = Color.White)

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
                { navController.navigate("history") },
                Modifier.fillMaxWidth()
            ) {
                Text("View History")
            }
        }
    }
}