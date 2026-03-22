package com.example.agriplus.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.agriplus.data.HistoryItem
import com.example.agriplus.data.RealtimeManager
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryScreen() {

    val manager = remember { RealtimeManager() }
    var list by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    val user = FirebaseAuth.getInstance().currentUser
    val profilePhoto = user?.photoUrl
    val username = user?.displayName ?: "Farmer"

    LaunchedEffect(Unit) {
        manager.getResults {
            list = it
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
        ) {

            // 🔥 PROFILE HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (profilePhoto != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profilePhoto),
                        contentDescription = null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = username,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔥 HISTORY LIST
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                itemsIndexed(list, key = { _, item -> item.timestamp }) { index, item ->

                    var expanded by remember { mutableStateOf(false) }
                    var offsetX by remember { mutableStateOf(0f) }

                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f
                    )

                    val date = SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                    ).format(Date(item.timestamp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .offset(x = offsetX.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    offsetX += dragAmount / 10

                                    // 👉 SWIPE TO DELETE
                                    if (offsetX < -120) {
                                        manager.deleteItem(item)
                                        list = list.toMutableList().apply { removeAt(index) }
                                    }
                                }
                            }
                    ) {

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(modifier = Modifier.padding(14.dp)) {

                                // 🔹 HEADER
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column {

                                        Text(
                                            text = item.result,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            text = date,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(onClick = {
                                        expanded = !expanded
                                    }) {
                                        Icon(
                                            Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(rotation)
                                        )
                                    }
                                }

                                // 🔥 EXPAND ANIMATION
                                AnimatedVisibility(
                                    visible = expanded,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {

                                    Column {

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // 🔥 CONFIDENCE BAR
                                        Text(
                                            text = "Confidence: ${(item.confidence * 100).toInt()}%",
                                            fontSize = 14.sp
                                        )

                                        LinearProgressIndicator(
                                            progress = item.confidence.toFloat(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            color = Color(0xFF4CAF50)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // 🔥 IMAGE
                                        if (item.imageUri.isNotEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(item.imageUri),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🔥 CLEAR ALL
            Button(
                onClick = {
                    manager.clearAll()
                    list = emptyList()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Clear All History")
            }
        }
    }
}