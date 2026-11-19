package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette
import com.example.agriplus.ui.theme.AppOutlinedTextFieldColors

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.treatment.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentGuideScreen(navController: NavController) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        TreatmentRepository.init(context)
    }

    var selectedCategory by remember { mutableStateOf(ConditionCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<TreatmentGuideItem?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val guides = remember(selectedCategory, searchQuery, refreshTrigger) {
        TreatmentRepository.searchGuides(searchQuery, selectedCategory)
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
                            text = "Treatment Guide & Library",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Offline Coffee Health Knowledgebase",
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

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search disease, symptom, or treatment...", color = AppPalette.muted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppPalette.muted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = AppPalette.muted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = AppOutlinedTextFieldColors()
            )

            // CATEGORY CHIPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    label = "All Guides (${TreatmentRepository.getAllGuides().size})",
                    selected = selectedCategory == ConditionCategory.ALL,
                    onClick = { selectedCategory = ConditionCategory.ALL }
                )
                CategoryChip(
                    label = "⭐ Favorites",
                    selected = selectedCategory == ConditionCategory.FAVORITES,
                    onClick = { selectedCategory = ConditionCategory.FAVORITES }
                )
                CategoryChip(
                    label = "🍂 Fungal Diseases",
                    selected = selectedCategory == ConditionCategory.FUNGAL_DISEASE,
                    onClick = { selectedCategory = ConditionCategory.FUNGAL_DISEASE }
                )
                CategoryChip(
                    label = "🐛 Pests",
                    selected = selectedCategory == ConditionCategory.PEST,
                    onClick = { selectedCategory = ConditionCategory.PEST }
                )
                CategoryChip(
                    label = "🧪 Deficiencies",
                    selected = selectedCategory == ConditionCategory.NUTRIENT_DEFICIENCY,
                    onClick = { selectedCategory = ConditionCategory.NUTRIENT_DEFICIENCY }
                )
            }

            // LIST OF GUIDES
            if (guides.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No treatment guides found",
                            color = AppPalette.muted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Try searching with a different term or category",
                            color = AppPalette.muted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(guides) { item ->
                        GuideCard(
                            item = item,
                            onClick = { selectedItem = item },
                            onFavoriteToggle = {
                                TreatmentRepository.toggleFavorite(item.id)
                                refreshTrigger++
                            }
                        )
                    }
                }
            }
        }
    }

    // DETAIL BOTTOM SHEET
    selectedItem?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            containerColor = AppPalette.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = AppPalette.blue) }
        ) {
            GuideDetailContent(
                item = item,
                onClose = { selectedItem = null },
                onScheduleReminder = {
                    selectedItem = null
                    navController.navigate("reminders")
                },
                onNoteSaved = { note ->
                    TreatmentRepository.saveUserNote(item.id, note)
                    refreshTrigger++
                }
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AppPalette.surfaceAlt else AppPalette.background,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, AppPalette.surfaceAlt)
    ) {
        Text(
            text = label,
            color = if (selected) AppPalette.text else AppPalette.accentText,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun GuideCard(
    item: TreatmentGuideItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = AppPalette.surfaceAlt,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = item.iconEmoji, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        color = AppPalette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFFFD54F) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = item.scientificName,
                    color = AppPalette.accent,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.summary,
                    color = AppPalette.muted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when (item.severity) {
                            "Severe Risk" -> Color(0xFFB71C1C).copy(alpha = 0.4f)
                            "Moderate to Severe" -> Color(0xFFE65100).copy(alpha = 0.4f)
                            "Optimal Health" -> AppPalette.surfaceAlt.copy(alpha = 0.4f)
                            else -> AppPalette.surfaceAlt.copy(alpha = 0.4f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.severity,
                            color = AppPalette.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "View Protocol →",
                        color = AppPalette.accentText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GuideDetailContent(
    item: TreatmentGuideItem,
    onClose: () -> Unit,
    onScheduleReminder: () -> Unit,
    onNoteSaved: (String) -> Unit
) {
    var userNote by remember { mutableStateOf(item.customUserNote) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.iconEmoji, fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.name,
                        color = AppPalette.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.scientificName,
                        color = AppPalette.accent,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // DESCRIPTION
        Text(
            text = item.description,
            color = AppPalette.text.copy(alpha = 0.9f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = AppPalette.text.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(14.dp))

        // SYMPTOMS
        DetailSection(title = "🔍 Observable Symptoms") {
            item.symptoms.forEach { symptom ->
                BulletItem(text = symptom, bulletColor = Color(0xFFFFB74D))
            }
        }

        // CAUSES
        DetailSection(title = "⚠️ Root Causes & Environmental Triggers") {
            item.causes.forEach { cause ->
                BulletItem(text = cause, bulletColor = Color(0xFFE57373))
            }
        }

        // STEP-BY-STEP TREATMENT
        DetailSection(title = "🧪 Treatment Procedures & Dosage") {
            item.treatmentSteps.forEach { step ->
                BulletItem(text = step, bulletColor = AppPalette.accent)
            }
        }

        // PREVENTION
        DetailSection(title = "🛡️ Prevention & Shade Management") {
            item.preventionTechniques.forEach { prev ->
                BulletItem(text = prev, bulletColor = AppPalette.blue)
            }
        }

        // FERTILIZER GUIDANCE
        DetailSection(title = "🌱 Fertilizer & Nutrient Protocol") {
            Text(
                text = item.fertilizerNutrientGuidance,
                color = AppPalette.muted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        // CHEMICALS VS ORGANIC
        DetailSection(title = "📦 Recommended Products") {
            Text(
                text = "Approved Fungicides / Products:",
                color = AppPalette.accentText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            item.recommendedChemicals.forEach { chem ->
                Text("• $chem", color = AppPalette.text.copy(alpha = 0.85f), fontSize = 12.sp)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Organic & Biological Controls:",
                color = AppPalette.accentText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            item.organicAlternatives.forEach { org ->
                Text("• $org", color = AppPalette.text.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }

        // PERSONAL ESTATE NOTE
        DetailSection(title = "📝 My Plantation Notes (Saved Locally)") {
            OutlinedTextField(
                value = userNote,
                onValueChange = {
                    userNote = it
                    onNoteSaved(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add custom note (e.g. Sprayed block 2 on 15 Oct)...", color = AppPalette.muted) },
                colors = AppOutlinedTextFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACTION BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onScheduleReminder,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Set Treatment Reminder", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onClose,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPalette.text)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = AppPalette.text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
fun BulletItem(text: String, bulletColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = bulletColor,
            modifier = Modifier
                .size(6.dp)
                .padding(top = 5.dp)
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = AppPalette.text.copy(alpha = 0.85f),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}
