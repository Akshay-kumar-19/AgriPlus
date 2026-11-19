package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette
import com.example.agriplus.ui.theme.AppOutlinedTextFieldColors

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val prefs = remember { context.getSharedPreferences("agriplus_user_profile_prefs", Context.MODE_PRIVATE) }

    var displayName by remember { mutableStateOf(user?.displayName ?: "Coffee Grower") }
    var primaryVariety by remember { mutableStateOf(prefs.getString("primary_variety", "Arabica (Selection 795)") ?: "Arabica (Selection 795)") }
    var primaryRegion by remember { mutableStateOf(prefs.getString("primary_region", "Chikkamagaluru, Karnataka") ?: "Chikkamagaluru, Karnataka") }
    var notifyRustAlerts by remember { mutableStateOf(prefs.getBoolean("notify_rust", true)) }
    var preScanQualityCheck by remember { mutableStateOf(prefs.getBoolean("pre_scan_check", true)) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppPalette.background, AppPalette.backgroundAlt, AppPalette.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // TOP BAR
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Farmer Profile & Settings",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Account & Plantation Preferences",
                            color = AppPalette.accentText,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppPalette.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // PROFILE HEADER CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user?.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = displayName,
                            color = AppPalette.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = user?.email ?: "Registered Coffee Planter",
                            color = AppPalette.accentText,
                            fontSize = 12.sp
                        )
                    }
                }

                // EDIT PROFILE DETAILS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Agronomic Details", color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Farmer Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        , colors = AppOutlinedTextFieldColors())

                        OutlinedTextField(
                            value = primaryVariety,
                            onValueChange = { primaryVariety = it },
                            label = { Text("Primary Coffee Variety") },
                            placeholder = { Text("e.g. Arabica Sln 795 / Chandragiri / Cauvery") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        , colors = AppOutlinedTextFieldColors())

                        OutlinedTextField(
                            value = primaryRegion,
                            onValueChange = { primaryRegion = it },
                            label = { Text("Growing District / Region") },
                            placeholder = { Text("e.g. Chikkamagaluru, Kodagu, Wayanad") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        , colors = AppOutlinedTextFieldColors())

                        Button(
                            onClick = {
                                isSaving = true
                                prefs.edit()
                                    .putString("primary_variety", primaryVariety)
                                    .putString("primary_region", primaryRegion)
                                    .apply()

                                if (displayName.isNotBlank()) {
                                    val req = UserProfileChangeRequest.Builder()
                                        .setDisplayName(displayName)
                                        .build()
                                    user?.updateProfile(req)?.addOnCompleteListener {
                                        isSaving = false
                                        statusMessage = "Profile updated successfully!"
                                    }
                                } else {
                                    isSaving = false
                                    statusMessage = "Preferences saved!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isSaving) "Saving..." else "Save Profile Preferences")
                        }

                        if (statusMessage.isNotEmpty()) {
                            Text(statusMessage, color = AppPalette.accentText, fontSize = 12.sp)
                        }
                    }
                }

                // APP PREFERENCES
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("App Preferences", color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Dark mode", color = AppPalette.text, fontSize = 13.sp)
                                Text("Turn off for a light appearance", color = AppPalette.muted, fontSize = 11.sp)
                            }
                            Switch(checked = darkTheme, onCheckedChange = onThemeChange)
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = AppPalette.text.copy(alpha = 0.06f))
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("High Rust & Fungal Alerts", color = AppPalette.text, fontSize = 13.sp)
                                Text("Notify when relative humidity exceeds 80%", color = AppPalette.muted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = notifyRustAlerts,
                                onCheckedChange = {
                                    notifyRustAlerts = it
                                    prefs.edit().putBoolean("notify_rust", it).apply()
                                }
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = AppPalette.text.copy(alpha = 0.06f))
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Pre-Scan Image Quality Checker", color = AppPalette.text, fontSize = 13.sp)
                                Text("Detect blur, darkness, and duplicate leaf submissions", color = AppPalette.muted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = preScanQualityCheck,
                                onCheckedChange = {
                                    preScanQualityCheck = it
                                    prefs.edit().putBoolean("pre_scan_check", it).apply()
                                }
                            )
                        }
                    }
                }

                // LOGOUT BUTTON
                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPalette.text)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out")
                }

                // DANGER ZONE (DELETE ACCOUNT)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Danger Zone", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Permanently delete your account, diagnostic scans, plantations, and reminders. This cannot be undone.",
                            color = AppPalette.muted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Account & All Data")
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // DELETE CONFIRMATION DIALOG
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Permanently Delete Account?", color = AppPalette.text, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you absolutely certain? This will wipe your history, plantation records, and Firebase account credentials completely.",
                    color = AppPalette.muted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            FirebaseDatabase.getInstance().reference.child("users").child(uid).removeValue()
                        }
                        auth.currentUser?.delete()?.addOnCompleteListener {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Confirm Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = AppPalette.muted)
                }
            },
            containerColor = AppPalette.surface
        )
    }
}
