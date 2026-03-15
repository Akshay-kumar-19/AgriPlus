package com.example.agriplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.agriplus.screens.HomeScreen
import com.example.agriplus.screens.LoginScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val account = GoogleSignIn.getLastSignedInAccount(this)

        val startDestination =
            if (account != null) "home" else "login"

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
            }
        }
    }
}