package com.example.agriplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.*
import com.example.agriplus.screens.HomeScreen
import com.example.agriplus.screens.LoginScreen
import com.example.agriplus.screens.HistoryScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 FULL SCREEN (hide status bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(android.view.WindowInsets.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 🔥 CORRECT LOGIN CHECK
        val user = FirebaseAuth.getInstance().currentUser

        val startDestination =
            if (user != null) "home" else "login"

        setContent {

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {

                composable("login") {
                    LoginScreen(navController)
                }

                composable("home") {
                    HomeScreen(navController)
                }

                composable("history") {
                    HistoryScreen()
                }
            }
        }
    }
}