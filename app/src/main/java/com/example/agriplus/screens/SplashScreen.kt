package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.agriplus.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.gplant)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    // 🔥 DELAY + NAVIGATION
    LaunchedEffect(Unit) {
        delay(2000)

        if (user != null) {
            navController.navigate("home") {
                popUpTo(0)
            }
        } else {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    // 🔥 UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppPalette.background, AppPalette.surface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            androidx.compose.material3.Text(
                text = "AgriPlus 🌿",
                color = AppPalette.text
            )
        }
    }
}