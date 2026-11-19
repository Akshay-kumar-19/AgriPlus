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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.plantation.PlantationManager
import com.example.agriplus.data.reminder.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(navController: NavController) {

    val context = LocalContext.current
    val manager = remember { ReminderManager(context) }
    val plantationManager = remember { PlantationManager(context) }

    var reminders by remember { mutableStateOf<List<TreatmentReminder>>(emptyList()) }
    var filterIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Upcoming, 2: Completed
    var showAddDialog by remember { mutableStateOf(false) }

    fun refresh() {
        manager.getReminders { list ->
            reminders = list
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val filteredList = remember(reminders, filterIndex) {
        when (filterIndex) {
            1 -> reminders.filter { !it.isCompleted }
            2 -> reminders.filter { it.isCompleted }
            else -> reminders
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
                            text = "Treatment Reminders",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Follow-up Sprays & Farm Operations",
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
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddAlarm,
                            contentDescription = "New Reminder",
                            tint = AppPalette.accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // FILTER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderFilterTab("All (${reminders.size})", selected = filterIndex == 0) { filterIndex = 0 }
                ReminderFilterTab("Upcoming (${reminders.count { !it.isCompleted }})", selected = filterIndex == 1) { filterIndex = 1 }
                ReminderFilterTab("Completed (${reminders.count { it.isCompleted }})", selected = filterIndex == 2) { filterIndex = 2 }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No Reminders Found", color = AppPalette.text, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Schedule spray schedules, plant checks, or re-scans to never miss an agronomy window.",
                            color = AppPalette.muted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt)
                        ) {
                            Text("+ Schedule Reminder")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { item ->
                        ReminderCard(
                            item = item,
                            onToggleComplete = {
                                manager.toggleComplete(item.id) { refresh() }
                            },
                            onDelete = {
                                manager.deleteReminder(item.id) { refresh() }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            defaultPlantation = plantationManager.getActivePlantation().name,
            onDismiss = { showAddDialog = false },
            onAdd = { newReminder ->
                manager.addReminder(newReminder) { refresh() }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderFilterTab(label: String, selected: Boolean, onClick: () -> Unit) {
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
fun ReminderCard(
    item: TreatmentReminder,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy • hh:mm a", Locale.getDefault())
    val dateText = dateFormat.format(Date(item.scheduledTimeMillis))
    val diffDays = ((item.scheduledTimeMillis - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

    val (badgeText, badgeColor) = when {
        item.isCompleted -> Pair("COMPLETED", AppPalette.blue)
        diffDays < 0 -> Pair("OVERDUE", Color(0xFFE53935))
        diffDays == 0 -> Pair("DUE TODAY", Color(0xFFFF9800))
        diffDays == 1 -> Pair("TOMORROW", AppPalette.blue)
        else -> Pair("IN $diffDays DAYS", AppPalette.accent)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) AppPalette.surface.copy(alpha = 0.7f) else AppPalette.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (item.isCompleted) AppPalette.blue else AppPalette.muted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = if (item.isCompleted) Color.Gray else AppPalette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "⏰ $dateText",
                    color = AppPalette.accentText,
                    fontSize = 11.sp
                )

                Text(
                    text = "🌱 ${item.plantationName} • ${item.type.name.replace("_", " ")}",
                    color = AppPalette.muted,
                    fontSize = 11.sp
                )

                if (item.notes.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        color = AppPalette.text.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    defaultPlantation: String,
    onDismiss: () -> Unit,
    onAdd: (TreatmentReminder) -> Unit
) {
    var title by remember { mutableStateOf("Bordeaux Mixture 0.5% Spray") }
    var diseaseName by remember { mutableStateOf("Coffee Leaf Rust") }
    var plantationName by remember { mutableStateOf(defaultPlantation) }
    var selectedType by remember { mutableStateOf(ReminderType.SPRAYING) }
    var daysAhead by remember { mutableIntStateOf(7) }
    var notes by remember { mutableStateOf("Target leaf undersides with fine mist spray nozzles.") }

    val presetTitles = listOf(
        "Bordeaux Mixture 0.5% Spray",
        "Hexaconazole 5% EC Spray",
        "Follow-up Leaf Rust Re-scan",
        "Post-Monsoon NPK Application",
        "Check Coffee Berry Borer Traps",
        "Inspect Leaf Undersides for Rust"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Schedule Treatment Reminder", color = AppPalette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())

                // Quick suggestions
                Text("Suggested Operations:", color = AppPalette.muted, fontSize = 11.sp)
                presetTitles.take(3).forEach { suggestion ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { title = suggestion },
                        color = AppPalette.background,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "+ $suggestion",
                            color = AppPalette.accentText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Schedule For:", color = AppPalette.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(3, 7, 14, 21).forEach { d ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { daysAhead = d },
                            shape = RoundedCornerShape(8.dp),
                            color = if (daysAhead == d) AppPalette.surfaceAlt else AppPalette.background
                        ) {
                            Text(
                                text = "$d Days",
                                color = AppPalette.text,
                                fontSize = 11.sp,
                                fontWeight = if (daysAhead == d) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = plantationName,
                    onValueChange = { plantationName = it },
                    label = { Text("Plantation / Block") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Application Notes / Instructions") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val scheduled = System.currentTimeMillis() + (daysAhead.toLong() * 24 * 60 * 60 * 1000)
                        val reminder = TreatmentReminder(
                            title = title.trim(),
                            diseaseName = diseaseName.trim(),
                            plantationName = plantationName.trim(),
                            type = selectedType,
                            scheduledTimeMillis = scheduled,
                            notes = notes.trim()
                        )
                        onAdd(reminder)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt)
            ) {
                Text("Set Alarm & Reminder")
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
