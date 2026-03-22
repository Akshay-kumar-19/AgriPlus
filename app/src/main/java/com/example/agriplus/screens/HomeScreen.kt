package com.example.agriplus.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.agriplus.R
import com.example.agriplus.data.HistoryItem
import com.example.agriplus.data.RealtimeManager
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
    var isScanning by remember { mutableStateOf(false) }

    // 🔥 LOTTIES
    val plantAnim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.plant))
    val scanAnim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan))

    val plantProgress by animateLottieCompositionAsState(plantAnim, iterations = LottieConstants.IterateForever)
    val scanProgress by animateLottieCompositionAsState(scanAnim, iterations = LottieConstants.IterateForever)

    // 📸 GALLERY
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            uri?.let {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
        }

    // 📷 CAMERA
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            bitmap = it
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
                            .size(50.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text("Welcome 👋", color = Color.LightGray, fontSize = 12.sp)

                        Text(
                            text = user?.displayName ?: "User",
                            color = Color.White,
                            fontSize = 20.sp,
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

            // 🔥 IMAGE / LOTTIE
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
                        // 🌱 BIG CENTER PLANT
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

            // 🔥 SCAN BUTTON (FIXED WITH SAVE)
            Button(
                onClick = {

                    bitmap?.let { bmp ->

                        isScanning = true
                        resultText = ""

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

                                // ✅🔥 SAVE HISTORY (THIS WAS MISSING)
                                manager.saveResult(
                                    HistoryItem(
                                        result = label,
                                        confidence = conf,
                                        imageUri = imageUri?.toString() ?: "",
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

            // 🔍 SCAN LOTTIE (BOTTOM)
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

            // 🔥 RESULT
            AnimatedVisibility(visible = resultText.isNotEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = resultText.replace("_", " "),
                            fontWeight = FontWeight.Bold
                        )

                        Text("Confidence: ${(confidence * 100).toInt()}%")
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