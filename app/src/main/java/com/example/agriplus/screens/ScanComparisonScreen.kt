package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.DiseaseRepository
import com.example.agriplus.data.HistoryItem
import com.example.agriplus.data.RealtimeManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanComparisonScreen(navController: NavController) {

    val manager = remember { RealtimeManager() }
    var historyList by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var selectedScanA by remember { mutableStateOf<HistoryItem?>(null) }
    var selectedScanB by remember { mutableStateOf<HistoryItem?>(null) }
    var pickingForA by remember { mutableStateOf(false) }
    var pickingForB by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        manager.getResults { list ->
            val sorted = list.sortedByDescending { it.timestamp }
            historyList = sorted
            if (sorted.size >= 2) {
                selectedScanB = sorted[0] // Most recent
                selectedScanA = sorted[1] // Previous
            } else if (sorted.size == 1) {
                selectedScanA = sorted[0]
            }
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
                            text = "Scan Comparison",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track Leaf Recovery Over Time",
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

            if (historyList.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Compare, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Need At Least 2 Scans", color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Perform multiple leaf scans over different days to compare diagnostic evolution and treatment efficacy.",
                            color = AppPalette.muted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // SELECTORS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScanSelectButton(
                            label = "Scan A (Baseline)",
                            item = selectedScanA,
                            modifier = Modifier.weight(1f),
                            onClick = { pickingForA = true }
                        )
                        ScanSelectButton(
                            label = "Scan B (Follow-up)",
                            item = selectedScanB,
                            modifier = Modifier.weight(1f),
                            onClick = { pickingForB = true }
                        )
                    }

                    // SIDE BY SIDE COMPARISON
                    if (selectedScanA != null && selectedScanB != null) {
                        val scanA = selectedScanA!!
                        val scanB = selectedScanB!!
                        val infoA = DiseaseRepository.getInfo(scanA.result)
                        val infoB = DiseaseRepository.getInfo(scanB.result)

                        val diffDays = abs((scanB.timestamp - scanA.timestamp) / (24 * 60 * 60 * 1000)).toInt()
                        val isHealthyA = scanA.result.lowercase().contains("healthy")
                        val isHealthyB = scanB.result.lowercase().contains("healthy")

                        // PROGRESSION BADGE
                        val (progressionTitle, progressionDesc, progressionColor) = when {
                            !isHealthyA && isHealthyB -> Triple(
                                "🎉 Significant Plant Recovery",
                                "The plant transitioned from ${infoA.name} to Healthy Foliage. The treatment protocol proved effective!",
                                AppPalette.blue
                            )
                            !isHealthyA && !isHealthyB && scanA.result == scanB.result -> Triple(
                                "⚠️ Condition Persisting",
                                "Active ${infoA.name} still detected after $diffDays days. Ensure fungicide spray reaches stomata on the lower leaf surface.",
                                Color(0xFFFFB74D)
                            )
                            isHealthyA && isHealthyB -> Triple(
                                "🌿 Continuous Clean Health",
                                "Both scans confirm healthy leaves without fungal or nutrient deficiencies.",
                                AppPalette.accent
                            )
                            else -> Triple(
                                "🔄 Condition Shift Observed",
                                "Diagnosis changed from ${infoA.name} to ${infoB.name}. Re-evaluate soil nutrition and moisture balance.",
                                AppPalette.blue
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = progressionColor.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, progressionColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(progressionTitle, color = progressionColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(progressionDesc, color = AppPalette.text.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 16.sp)
                                Spacer(Modifier.height(6.dp))
                                Text("Time Elapsed: $diffDays days between scans", color = AppPalette.muted, fontSize = 11.sp)
                            }
                        }

                        // IMAGES SIDE BY SIDE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScanVisualCard(
                                title = "Scan A",
                                item = scanA,
                                info = infoA,
                                bitmap = base64ToBitmap(scanA.imageBase64),
                                modifier = Modifier.weight(1f)
                            )
                            ScanVisualCard(
                                title = "Scan B",
                                item = scanB,
                                info = infoB,
                                bitmap = base64ToBitmap(scanB.imageBase64),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // DETAILED COMPARISON TABLE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Comparison Breakdown", color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))

                                CompareRow(
                                    label = "Diagnosis",
                                    valA = infoA.name,
                                    valB = infoB.name
                                )
                                CompareRow(
                                    label = "Confidence",
                                    valA = "${(scanA.confidence * 100).toInt()}%",
                                    valB = "${(scanB.confidence * 100).toInt()}%"
                                )
                                CompareRow(
                                    label = "Cause",
                                    valA = infoA.cause,
                                    valB = infoB.cause
                                )
                                CompareRow(
                                    label = "Primary Action",
                                    valA = infoA.treatment.take(60) + "...",
                                    valB = infoB.treatment.take(60) + "..."
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // PICKER DIALOG FOR SCAN A OR B
    if (pickingForA || pickingForB) {
        val isA = pickingForA
        AlertDialog(
            onDismissRequest = {
                pickingForA = false
                pickingForB = false
            },
            title = {
                Text(
                    text = if (isA) "Select Scan A (Baseline)" else "Select Scan B (Follow-up)",
                    color = AppPalette.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyList) { item ->
                        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                        val info = DiseaseRepository.getInfo(item.result)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isA) selectedScanA = item else selectedScanB = item
                                    pickingForA = false
                                    pickingForB = false
                                },
                            colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(info.name, color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(date, color = AppPalette.accentText, fontSize = 11.sp)
                                    Text("Confidence: ${(item.confidence * 100).toInt()}%", color = AppPalette.muted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    pickingForA = false
                    pickingForB = false
                }) {
                    Text("Close", color = AppPalette.muted)
                }
            },
            containerColor = AppPalette.surface
        )
    }
}

@Composable
fun ScanSelectButton(
    label: String,
    item: HistoryItem?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, color = AppPalette.accentText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = item?.result ?: "Select Scan",
                color = AppPalette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("Tap to change", color = AppPalette.muted, fontSize = 9.sp)
        }
    }
}

@Composable
fun ScanVisualCard(
    title: String,
    item: HistoryItem,
    info: com.example.agriplus.data.DiseaseInfo,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(title, color = AppPalette.accentText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(
                text = info.name,
                color = AppPalette.text,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("Conf: ${(item.confidence * 100).toInt()}%", color = Color(0xFFFFD54F), fontSize = 11.sp)
            Text(date, color = AppPalette.muted, fontSize = 9.sp)
        }
    }
}

@Composable
fun CompareRow(label: String, valA: String, valB: String) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(label, color = AppPalette.accentText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = AppPalette.background,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(valA, color = AppPalette.text, fontSize = 11.sp, modifier = Modifier.padding(6.dp))
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = AppPalette.background,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(valB, color = AppPalette.text, fontSize = 11.sp, modifier = Modifier.padding(6.dp))
            }
        }
    }
}
