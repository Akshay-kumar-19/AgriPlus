package com.example.agriplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.*
import com.example.agriplus.screens.*
import com.example.agriplus.ui.theme.AgriPlusTheme
import com.example.agriplus.ui.theme.AppPalette

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

            val context = LocalContext.current
            val preferences = remember { context.getSharedPreferences("agriplus_user_profile_prefs", MODE_PRIVATE) }
            var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", true)) }
            LaunchedEffect(darkTheme) { AppPalette.dark = darkTheme }

            val navController = rememberNavController()

            AgriPlusTheme(darkTheme = darkTheme) {
            Column(Modifier.fillMaxSize().background(AppPalette.background)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(46.dp).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (darkTheme) "Dark" else "Light", color = AppPalette.text, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = {
                        darkTheme = it
                        preferences.edit().putBoolean("dark_theme", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppPalette.blue,
                            uncheckedThumbColor = AppPalette.blue,
                            uncheckedTrackColor = AppPalette.backgroundAlt,
                            uncheckedBorderColor = AppPalette.line
                        )
                    )
                }
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.weight(1f)
                ) {

                // 🔥 SPLASH SCREEN
                composable("splash") {
                    SplashScreen(navController)
                }

                // 🔥 LOGIN
                composable("login") {
                    LoginScreen(navController)
                }

                // 🔥 HOME & AI SCANNER
                composable("home") {
                    HomeScreen(navController)
                }

                // 📊 HEALTH ANALYTICS DASHBOARD
                composable("dashboard") {
                    AnalyticsDashboardScreen(navController)
                }

                // 🌱 MY PLANTATION MANAGEMENT
                composable("plantation") {
                    PlantationScreen(navController)
                }

                // 📖 TREATMENT GUIDE & OFFLINE LIBRARY
                composable("treatment_guide") {
                    TreatmentGuideScreen(navController)
                }

                // ⏰ TREATMENT REMINDERS
                composable("reminders") {
                    RemindersScreen(navController)
                }

                // 🕒 SCAN HISTORY
                composable("history") {
                    HistoryScreen(navController)
                }

                // 🔄 SCAN COMPARISON
                composable("comparison") {
                    ScanComparisonScreen(navController)
                }

                // 👨‍🌾 EXPERT CONSULTATION
                composable("consultation") {
                    ExpertConsultationScreen(navController)
                }

                // ⛅ WEATHER & AGRO ADVISORY
                composable("weather") {
                    WeatherScreen(navController)
                }

                // ⚙️ PROFILE & SETTINGS
                composable("profile") {
                    ProfileScreen(navController, darkTheme = darkTheme, onThemeChange = {
                        darkTheme = it
                        preferences.edit().putBoolean("dark_theme", it).apply()
                    })
                }
                }
            }
            }
        }
    }
}
