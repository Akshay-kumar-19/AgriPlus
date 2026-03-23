package com.example.agriplus.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.agriplus.data.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryScreen() {

    val manager = remember { RealtimeManager() }
    var list by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    val user = FirebaseAuth.getInstance().currentUser

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        manager.getResults {
            list = it.sortedByDescending { item -> item.timestamp }
        }
    }

    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
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
                .padding(16.dp)
                .navigationBarsPadding()   // 🔥 FIX overlap
        ) {

            // 🔥 HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text("History", color = Color.LightGray, fontSize = 12.sp)

                    Text(
                        text = user?.displayName ?: "User",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                itemsIndexed(list, key = { _, item -> item.timestamp }) { index, item ->

                    var expanded by remember { mutableStateOf(false) }
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

                    val info = DiseaseRepository.getInfo(item.result)

                    val date = SimpleDateFormat(
                        "dd MMM yyyy • hh:mm a",
                        Locale.getDefault()
                    ).format(Date(item.timestamp))

                    val isHealthy = item.result.contains("healthy", true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHealthy)
                                Color(0xFF1B5E20)
                            else
                                Color(0xFF1E1E1E)
                        )
                    ) {

                        Column(modifier = Modifier.padding(14.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        text = info.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Text(
                                        text = date,
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )
                                }

                                Row {

                                    IconButton(onClick = {
                                        manager.deleteItem(item)
                                        list = list.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.White)
                                    }

                                    IconButton(onClick = {
                                        expanded = !expanded
                                    }) {
                                        Icon(
                                            Icons.Default.ExpandMore,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.rotate(rotation)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { item.confidence.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Color.Green,
                                trackColor = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Confidence ${(item.confidence * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp
                            )

                            AnimatedVisibility(
                                visible = expanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {

                                Column {

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val bmp = base64ToBitmap(item.imageBase64)

                                    bmp?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Cause:", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(info.cause, color = Color.White)

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("Symptoms:", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(info.symptoms, color = Color.White)

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("Treatment:", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(info.treatment, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 🔥 CLEAR BUTTON (SAFE)
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear All History")
            }
        }

        // 🔥 CONFIRMATION DIALOG
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Clear History") },
                text = { Text("Are you sure you want to delete all history?") },
                confirmButton = {
                    TextButton(onClick = {
                        manager.clearAll()
                        list = emptyList()
                        showDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}