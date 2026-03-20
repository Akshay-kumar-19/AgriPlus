package com.example.agriplus.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import com.example.agriplus.R
import com.example.agriplus.ml.LeafClassifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.example.agriplus.data.HistoryManager
import com.example.agriplus.data.ScanResult
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val classifier = remember { LeafClassifier(context) }
    val labels = remember { classifier.loadLabels(context) }

    val account = GoogleSignIn.getLastSignedInAccount(context)
    val username = account?.displayName ?: "User"
    val profileImage = account?.photoUrl

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    /* ---------- GALLERY ---------- */
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val stream = context.contentResolver.openInputStream(it)
                val temp = BitmapFactory.decodeStream(stream)
                bitmap = temp?.copy(Bitmap.Config.ARGB_8888, true)
            }
        }

    /* ---------- CAMERA ---------- */
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
            bmp?.let {
                bitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Image(
                    painter = rememberAsyncImagePainter(profileImage),
                    contentDescription = null,
                    modifier = Modifier.size(45.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text("Hello, $username 👋")
                    Text("Let’s check your crop 🌿")
                }
            }

            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ---------- IMAGE ---------- */
        Card(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val comp by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.plant)
                    )
                    LottieAnimation(comp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ---------- BUTTONS ---------- */
        Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Upload from Gallery")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.fillMaxWidth()) {
            Text("Take Photo")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            if (bitmap == null) return@Button

            isLoading = true

            val prediction =
                classifier.classify(bitmap!!, labels)
                    .replace("_", " ")
                    .replaceFirstChar { it.uppercase() }

            result = prediction
            isLoading = false
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Scan Leaf")
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ---------- LOADING ---------- */
        if (isLoading) {
            val comp by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading)
            )
            LottieAnimation(comp, modifier = Modifier.size(120.dp))
        }

        /* ---------- RESULT CARD ---------- */
        if (result.isNotEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "🌿 $result",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Tap to view more details",
                        color = Color.Gray,
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        })

                    AnimatedVisibility(visible = expanded) {

                        Column {

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("💊 Treatment:")
                            Text("Detailed treatment will be added here.")

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("⚠ Severity: Medium")
                            Text("📅 Recommended action: Weekly care")

                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("More Features Coming Soon")
        }
    }
}