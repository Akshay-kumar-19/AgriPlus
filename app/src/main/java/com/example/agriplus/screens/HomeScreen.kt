package com.example.agriplus.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current

    val classifier = remember { LeafClassifier(context) }
    val labels = remember { classifier.loadLabels(context) }

    val account = GoogleSignIn.getLastSignedInAccount(context)
    val username = account?.displayName ?: "User"
    val profileImage = account?.photoUrl

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("") }

    /* ---------------- GALLERY ---------------- */

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempBitmap = BitmapFactory.decodeStream(inputStream)

                // 🔥 FIX: Convert to ARGB_8888
                bitmap = tempBitmap?.copy(Bitmap.Config.ARGB_8888, true)
            }
        }

    /* ---------------- CAMERA ---------------- */

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { capturedBitmap ->

            capturedBitmap?.let {
                // 🔥 FIX: Convert to ARGB_8888
                bitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF6F6F6))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Spacer(modifier = Modifier.height(10.dp))

            /* ---------------- TOP BAR ---------------- */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Image(
                        painter = rememberAsyncImagePainter(profileImage),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Welcome, $username 👋",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()

                        GoogleSignIn.getClient(
                            context,
                            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                        ).signOut()

                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            /* ---------------- IMAGE PREVIEW ---------------- */

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (bitmap != null) {

                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Leaf",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                    } else {

                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.plant)
                        )

                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            /* ---------------- BUTTONS ---------------- */

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Upload from Gallery")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    if (bitmap == null) {
                        resultText = "Please select image first"
                        return@Button
                    }

                    try {
                        val prediction =
                            classifier.classify(bitmap!!, labels)
                                .replace("_", " ")
                                .replaceFirstChar { it.uppercase() }

                        resultText = "Detected: $prediction"

                    } catch (e: Exception) {
                        e.printStackTrace()
                        resultText = "Prediction error"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Scan Leaf")
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (resultText.isNotEmpty()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        /* ---------------- BOTTOM BUTTON ---------------- */

        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("More Features (Coming Soon)")
        }
    }
}