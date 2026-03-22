package com.example.agriplus.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    var resultText by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf(0.0) }
    var diseaseInfo by remember { mutableStateOf<DiseaseInfo?>(null) }

    var isScanning by remember { mutableStateOf(false) }

    // 🔥 LOTTIES
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

    // 🔥 BASE64 FUNCTION
    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    // 🎨 BACKGROUND
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
        ) {

            // 🔥 HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text("Welcome 👋", color = Color.LightGray, fontSize = 12.sp)

                        Text(
                            text = user?.displayName ?: "User",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = {
                    auth.signOut()
                    navController.navigate("login")
                }) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // 🔥 IMAGE / LOTTIE BANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {

                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LottieAnimation(
                            composition = plantAnim,
                            progress = { plantProgress },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔥 BUTTONS
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Upload Image")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔥 SCAN BUTTON
            Button(
                onClick = {

                    bitmap?.let { bmp ->

                        isScanning = true
                        diseaseInfo = null

                        val stream = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val byteArray = stream.toByteArray()

                        ApiClient.uploadImage(byteArray) { response ->

                            isScanning = false

                            try {
                                val obj = JSONObject(response)
                                    .getJSONArray("predictions")
                                    .getJSONObject(0)

                                val label = obj.getString("class")
                                val conf = obj.getDouble("confidence")

                                resultText = label
                                confidence = conf

                                // 🔥 GET DISEASE INFO
                                diseaseInfo = DiseaseRepository.getInfo(label)

                                // 🔥 SAVE HISTORY (BASE64)
                                val base64Image = bitmapToBase64(bmp)

                                manager.saveResult(
                                    HistoryItem(
                                        result = label,
                                        confidence = conf,
                                        imageBase64 = base64Image,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )

                            } catch (e: Exception) {
                                resultText = "Error"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Scan Leaf")
            }

            // 🔍 SCAN LOTTIE
            AnimatedVisibility(visible = isScanning) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    LottieAnimation(
                        composition = scanAnim,
                        progress = { scanProgress },
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // 🔥 RESULT CARD (FIXED)
            AnimatedVisibility(visible = diseaseInfo != null) {

                val info = diseaseInfo!!
                val isHealthy = resultText.contains("healthy", true)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHealthy)
                            Color(0xFF1B5E20)
                        else
                            Color(0xFFB71C1C)
                    )
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = info.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { confidence.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Confidence ${(confidence * 100).toInt()}%",
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Cause:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(info.cause, color = Color.White)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Symptoms:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(info.symptoms, color = Color.White)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Treatment:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(info.treatment, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View History")
            }
        }
    }
}