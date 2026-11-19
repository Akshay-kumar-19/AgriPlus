package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.DiseaseRepository
import com.example.agriplus.data.HistoryItem
import com.example.agriplus.data.RealtimeManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(navController: NavController) {

    val manager = remember { RealtimeManager() }
    var historyItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTimeframe by remember { mutableIntStateOf(0) } // 0: All, 1: 30 Days, 2: 7 Days

    LaunchedEffect(Unit) {
        manager.getResults { list ->
            historyItems = list
            isLoading = false
        }
    }

    // Filter items based on timeframe
    val now = System.currentTimeMillis()
    val filteredItems = remember(historyItems, selectedTimeframe) {
        when (selectedTimeframe) {
            1 -> historyItems.filter { now - it.timestamp <= 30L * 24 * 60 * 60 * 1000 }
            2 -> historyItems.filter { now - it.timestamp <= 7L * 24 * 60 * 60 * 1000 }
            else -> historyItems
        }
    }

    val totalScans = filteredItems.size
    val healthyScans = filteredItems.count { it.result.lowercase().contains("healthy") }
    val affectedScans = totalScans - healthyScans
    val healthyPercent = if (totalScans > 0) ((healthyScans.toFloat() / totalScans) * 100).toInt() else 100

    // Condition frequency map
    val conditionCounts = remember(filteredItems) {
        filteredItems.groupBy { it.result.lowercase().trim() }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    val topCondition = conditionCounts.firstOrNull { !it.first.contains("healthy") } ?: conditionCounts.firstOrNull()

    // Categorization: Deficiency vs Disease
    val deficiencies = listOf("nitrogen", "phosphorus", "potassium", "calcium", "iron", "magnesium", "manganese", "boron")
    val deficiencyCount = filteredItems.count { item -> deficiencies.any { item.result.lowercase().contains(it) } }
    val fungalCount = filteredItems.count { item ->
        val r = item.result.lowercase()
        r.contains("rust") || r.contains("phoma") || r.contains("rot")
    }
    val pestCount = filteredItems.count { item -> item.result.lowercase().contains("mite") }

    // Average confidence
    val avgConfidence = if (totalScans > 0) (filteredItems.map { it.confidence }.average() * 100).toInt() else 0

    // Health Grade
    val (healthGrade, gradeColor, gradeAdvice) = when {
        totalScans == 0 -> Triple("N/A", Color.Gray, "Perform scans to calculate plantation health index.")
        healthyPercent >= 75 -> Triple("Grade A (Excellent)", AppPalette.blue, "Outstanding plant health. Continue current cultural hygiene.")
        healthyPercent >= 50 -> Triple("Grade B (Fair)", Color(0xFFFFB74D), "Moderate disease pressure. Inspect leaf undersides and ensure shade regulation.")
        else -> Triple("Grade C (Critical Alert)", Color(0xFFE53935), "High disease outbreak detected! Initiate recommended treatment sprays promptly.")
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
                            text = "Plant Health Analytics",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aggregated Farm Diagnostic Statistics",
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

            // TIMEFRAME FILTER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTab("All Time", selected = selectedTimeframe == 0) { selectedTimeframe = 0 }
                FilterTab("Past 30 Days", selected = selectedTimeframe == 1) { selectedTimeframe = 1 }
                FilterTab("Past 7 Days", selected = selectedTimeframe == 2) { selectedTimeframe = 2 }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPalette.blue)
                }
            } else if (totalScans == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No Scan Records Yet", color = AppPalette.text, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Scan leaf images with the AI scanner to generate real-time agronomic analytics.",
                            color = AppPalette.muted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // 1. HEALTH GRADE & HEALTHY RATIO CARD
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Plantation Health Index", color = AppPalette.muted, fontSize = 12.sp)
                                        Text(healthGrade, color = gradeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = gradeColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("$healthyPercent%", color = gradeColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Dual Progress Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(AppPalette.background)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(healthyScans.toFloat().coerceAtLeast(0.01f))
                                            .background(AppPalette.blue)
                                    )
                                    if (affectedScans > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(affectedScans.toFloat())
                                                .background(Color(0xFFE53935))
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🌿 Healthy: $healthyScans ($healthyPercent%)", color = AppPalette.accentText, fontSize = 11.sp)
                                    Text("⚠️ Affected: $affectedScans (${100 - healthyPercent}%)", color = Color(0xFFFF8A80), fontSize = 11.sp)
                                }

                                Spacer(Modifier.height(10.dp))
                                Text(gradeAdvice, color = AppPalette.muted, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }

                    // 2. QUICK STATS 4-GRID
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatBox(
                                title = "Total Scans",
                                value = totalScans.toString(),
                                subtitle = "Diagnostic samples",
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CameraAlt,
                                iconTint = AppPalette.accent
                            )
                            StatBox(
                                title = "Avg Confidence",
                                value = "$avgConfidence%",
                                subtitle = "Model certainty",
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Verified,
                                iconTint = AppPalette.blue
                            )
                        }
                    }

                    // 3. MOST FREQUENTLY DETECTED CONDITION
                    if (topCondition != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppPalette.surface,
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFFFB74D))
                                        }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Predominant Field Condition", color = AppPalette.muted, fontSize = 11.sp)
                                        Text(
                                            text = DiseaseRepository.getInfo(topCondition.first).name,
                                            color = AppPalette.text,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        val percent = ((topCondition.second.toFloat() / totalScans) * 100).toInt()
                                        Text(
                                            text = "${topCondition.second} scans ($percent% of all recorded symptoms)",
                                            color = AppPalette.accentText,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. DISEASE / DEFICIENCY CATEGORY BREAKDOWN
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Category Distribution",
                                    color = AppPalette.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(12.dp))

                                DistributionRow(label = "🍂 Fungal Invasions (Rust/Phoma)", count = fungalCount, total = totalScans, color = Color(0xFFE57373))
                                DistributionRow(label = "🧪 Nutrient Deficiencies (N/P/K/Fe)", count = deficiencyCount, total = totalScans, color = Color(0xFFFFB74D))
                                DistributionRow(label = "🐛 Insect / Mite Infestations", count = pestCount, total = totalScans, color = Color(0xFFBA68C8))
                                DistributionRow(label = "🌿 Fully Healthy Foliage", count = healthyScans, total = totalScans, color = AppPalette.accent)
                            }
                        }
                    }

                    // 5. DETAILED CONDITION FREQUENCY LIST
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "All Diagnosed Conditions",
                                    color = AppPalette.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(10.dp))

                                conditionCounts.forEach { entry ->
                                    val info = DiseaseRepository.getInfo(entry.first)
                                    val percent = ((entry.second.toFloat() / totalScans) * 100).toInt()

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = info.name,
                                            color = AppPalette.text,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${entry.second} scans ($percent%)",
                                            color = AppPalette.accentText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { entry.second.toFloat() / totalScans },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = AppPalette.blue,
                                        trackColor = AppPalette.background
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) AppPalette.surfaceAlt else AppPalette.background
    ) {
        Text(
            text = label,
            color = if (selected) AppPalette.text else AppPalette.muted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = AppPalette.muted, fontSize = 11.sp)
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(subtitle, color = AppPalette.accentText, fontSize = 10.sp)
        }
    }
}

@Composable
fun DistributionRow(label: String, count: Int, total: Int, color: Color) {
    val pct = if (total > 0) ((count.toFloat() / total) * 100).toInt() else 0
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = AppPalette.text.copy(alpha = 0.9f), fontSize = 12.sp)
            Text("$count ($pct%)", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = AppPalette.background
        )
    }
}
