package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette
import com.example.agriplus.ui.theme.AppOutlinedTextFieldColors

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.consultation.*
import com.example.agriplus.data.plantation.PlantationManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertConsultationScreen(navController: NavController) {

    val context = LocalContext.current
    val manager = remember { ConsultationManager(context) }
    val plantationManager = remember { PlantationManager(context) }

    var consultations by remember { mutableStateOf<List<ExpertConsultation>>(emptyList()) }
    var showAskDialog by remember { mutableStateOf(false) }

    fun refresh() {
        manager.getConsultations { list ->
            consultations = list
        }
    }

    LaunchedEffect(Unit) {
        refresh()
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
                            text = "Expert Consultation",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Direct Extension Desk & Agronomy Advice",
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
                actions = {
                    IconButton(onClick = { showAskDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "Ask Question",
                            tint = AppPalette.accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // BANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppPalette.surfaceAlt,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = AppPalette.text)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Coffee Agronomist Extension Desk", color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Have an atypical leaf spot or fertilizer issue? Submit symptoms and receive specialist agronomic advice.",
                            color = AppPalette.muted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // CONSULTATION LIST
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(consultations) { c ->
                    ConsultationCard(c)
                }
            }
        }
    }

    if (showAskDialog) {
        AskQuestionDialog(
            estateName = plantationManager.getActivePlantation().name,
            onDismiss = { showAskDialog = false },
            onSubmit = { newQuery ->
                manager.submitConsultation(newQuery) { refresh() }
                showAskDialog = false
            }
        )
    }
}

@Composable
fun ConsultationCard(c: ExpertConsultation) {
    val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(c.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AppPalette.surfaceAlt.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = c.category,
                        color = AppPalette.accentText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(date, color = AppPalette.muted, fontSize = 10.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "“${c.question}”",
                color = AppPalette.text,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = AppPalette.text.copy(alpha = 0.08f))
            Spacer(Modifier.height(10.dp))

            // EXPERT REPLY
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = CircleShape,
                    color = AppPalette.blue,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = AppPalette.text, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = c.agronomistName,
                        color = AppPalette.accentText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = c.agronomistReply,
                        color = AppPalette.text.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AskQuestionDialog(
    estateName: String,
    onDismiss: () -> Unit,
    onSubmit: (ExpertConsultation) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Disease Diagnosis & Rust Control") }
    val categories = listOf(
        "Disease Diagnosis & Rust Control",
        "Nutrient Deficiency & Soil Liming",
        "Pest Outbreak (Berry Borer / Mites)",
        "Foliar Spraying Suitability",
        "Shade Regulation & Pruning"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ask Coffee Agronomist", color = AppPalette.text, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Category:", color = AppPalette.muted, fontSize = 12.sp)
                categories.take(3).forEach { cat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategory == cat) AppPalette.surfaceAlt else AppPalette.background
                    ) {
                        Text(
                            text = cat,
                            color = AppPalette.text,
                            fontSize = 11.sp,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Describe Symptoms / Question") },
                    placeholder = { Text("e.g. Yellow lesions on upper leaves, orange powder underneath...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        onSubmit(
                            ExpertConsultation(
                                farmerName = "Coffee Grower",
                                estateName = estateName,
                                category = selectedCategory,
                                question = question.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt)
            ) {
                Text("Submit Query")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppPalette.muted)
            }
        },
        containerColor = AppPalette.surface
    )
}
