package com.example.agriplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.*
import com.example.agriplus.screens.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 FULL SCREEN
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

// 🔥 HIDE STATUS BAR (COMPATIBLE)
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "splash" // 🔥 START FROM SPLASH
            ) {

                // 🔥 SPLASH SCREEN
                composable("splash") {
                    SplashScreen(navController)
                }

                // 🔥 LOGIN
                composable("login") {
                    LoginScreen(navController)
                }

                // 🔥 HOME
                composable("home") {
                    HomeScreen(navController)
                }

                // 🔥 HISTORY
                composable("history") {
                    HistoryScreen()
                }
            }
        }
    }
}