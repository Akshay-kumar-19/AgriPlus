package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette
import com.example.agriplus.ui.theme.AppOutlinedTextFieldColors

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.agriplus.data.*
import com.example.agriplus.utils.PdfReportGenerator
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController? = null) {

    val context = LocalContext.current
    val manager = remember { RealtimeManager() }
    var rawList by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    val user = FirebaseAuth.getInstance().currentUser

    var showClearDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableIntStateOf(0) } // 0: All, 1: Healthy, 2: Diseases, 3: Deficiencies

    LaunchedEffect(Unit) {
        manager.getResults {
            rawList = it.sortedByDescending { item -> item.timestamp }
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

    val deficiencies = listOf("nitrogen", "phosphorus", "potassium", "calcium", "iron", "magnesium", "manganese", "boron")

    val filteredList = remember(rawList, searchQuery, filterCategory) {
        val q = searchQuery.trim().lowercase()
        rawList.filter { item ->
            val matchesCategory = when (filterCategory) {
                1 -> item.result.lowercase().contains("healthy")
                2 -> !item.result.lowercase().contains("healthy") && deficiencies.none { item.result.lowercase().contains(it) }
                3 -> deficiencies.any { item.result.lowercase().contains(it) }
                else -> true
            }

            val matchesSearch = if (q.isEmpty()) true else {
                item.result.lowercase().contains(q) ||
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.timestamp)).lowercase().contains(q)
            }

            matchesCategory && matchesSearch
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
                            text = "Diagnostic History",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${rawList.size} Saved Scans",
                            color = AppPalette.accentText,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppPalette.text)
                        }
                    }
                },
                actions = {
                    if (navController != null) {
                        IconButton(onClick = { navController.navigate("comparison") }) {
                            Icon(Icons.Default.Compare, contentDescription = "Compare", tint = AppPalette.accent)
                        }
                    }
                    if (rawList.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color(0xFFEF5350))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search by disease or date...", color = AppPalette.muted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppPalette.muted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = AppPalette.muted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = AppOutlinedTextFieldColors()
            )

            // FILTER TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilterChip("All (${rawList.size})", selected = filterCategory == 0) { filterCategory = 0 }
                HistoryFilterChip("🌿 Healthy", selected = filterCategory == 1) { filterCategory = 1 }
                HistoryFilterChip("🍂 Diseases", selected = filterCategory == 2) { filterCategory = 2 }
                HistoryFilterChip("🧪 Deficiencies", selected = filterCategory == 3) { filterCategory = 3 }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No scan records found", color = AppPalette.text, fontWeight = FontWeight.Bold)
                        Text("Try clearing your search or filter", color = AppPalette.muted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { item ->
                        val date = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                        val info = DiseaseRepository.getInfo(item.result)
                        val isHealthy = item.result.lowercase().contains("healthy")
                        val bmp = remember(item.imageBase64) { base64ToBitmap(item.imageBase64) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.DarkGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (bmp != null) {
                                            Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = info.name,
                                                color = AppPalette.text,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            IconButton(
                                                onClick = { itemToDelete = item },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Text(
                                            text = "Confidence: ${(item.confidence * 100).toInt()}% • ${if (isHealthy) "Healthy" else "Infected"}",
                                            color = if (isHealthy) AppPalette.accentText else Color(0xFFFF8A80),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(date, color = AppPalette.muted, fontSize = 10.sp)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = AppPalette.text.copy(alpha = 0.06f))
                                Spacer(Modifier.height(8.dp))

                                // ACTION BUTTONS (PDF & SHARE)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val pdf = PdfReportGenerator.generateScanReportPdf(context, item)
                                            if (pdf != null) {
                                                PdfReportGenerator.viewPdfReport(context, pdf)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPalette.accentText),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("PDF Report", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val pdf = PdfReportGenerator.generateScanReportPdf(context, item)
                                            if (pdf != null) {
                                                PdfReportGenerator.sharePdfReport(context, pdf)
                                            } else {
                                                // Fallback text share
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "AgriPlus Leaf Diagnosis")
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "AgriPlus Diagnosis: ${info.name}\nConfidence: ${(item.confidence * 100).toInt()}%\nCause: ${info.cause}\nTreatment: ${info.treatment}"
                                                    )
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Scan"))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Share", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CONFIRM DELETE SINGLE ITEM
    itemToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Scan Record?", color = AppPalette.text, fontWeight = FontWeight.Bold) },
            text = { Text("Remove this scan diagnosis from your history?", color = AppPalette.muted) },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deleteItem(target)
                        rawList = rawList.filter { it.timestamp != target.timestamp }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel", color = AppPalette.muted) }
            },
            containerColor = AppPalette.surface
        )
    }

    // CONFIRM CLEAR ALL
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?", color = AppPalette.text, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all diagnostic records from Firebase and this device.", color = AppPalette.muted) },
            confirmButton = {
                Button(
                    onClick = {
                        manager.clearHistory()
                        rawList = emptyList()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = AppPalette.muted) }
            },
            containerColor = AppPalette.surface
        )
    }
}

@Composable
fun HistoryFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
