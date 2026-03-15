package com.example.agriplus.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.agriplus.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = context as Activity

    // Google Sign-In configuration
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    // Launcher for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        Log.d("LOGIN_DEBUG", "Result received")

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {

            val account = task.getResult(ApiException::class.java)

            Log.d("LOGIN_DEBUG", "Login success: ${account.email}")

            // Navigate to HomeScreen
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }

        } catch (e: ApiException) {

            Log.d("LOGIN_DEBUG", "Login failed: ${e.statusCode}")

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        val composition = rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.plant)
        )

        LottieAnimation(
            composition = composition.value,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = "AgriPlus",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Smart Coffee Leaf Disease Detection"
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {

                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {

            Text(text = "Sign in with Google")

        }

    }
}