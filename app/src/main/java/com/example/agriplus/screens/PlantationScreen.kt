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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.plantation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantationScreen(navController: NavController) {

    val context = LocalContext.current
    val manager = remember { PlantationManager(context) }

    var plantations by remember { mutableStateOf<List<Plantation>>(emptyList()) }
    var activePlantation by remember { mutableStateOf(manager.getActivePlantation()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPlantation by remember { mutableStateOf<Plantation?>(null) }
    var itemToDelete by remember { mutableStateOf<Plantation?>(null) }

    LaunchedEffect(Unit) {
        manager.getPlantations { list ->
            plantations = list
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
                            text = "My Plantations",
                            color = AppPalette.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estate Management & Crop Tracking",
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
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Plantation",
                            tint = AppPalette.accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // ACTIVE ESTATE BANNER
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Park, contentDescription = null, tint = AppPalette.text)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ACTIVE SCAN ESTATE",
                                color = AppPalette.accentText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = activePlantation.name,
                            color = AppPalette.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activePlantation.location} • ${activePlantation.variety}",
                            color = AppPalette.muted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // LIST OF PLANTATIONS
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plantations) { p ->
                    val isActive = p.id == activePlantation.id || (p.name == activePlantation.name && p.location == activePlantation.location)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) AppPalette.surfaceAlt else AppPalette.surface
                        ),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, AppPalette.blue) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = p.name,
                                            color = AppPalette.text,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        if (isActive) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                color = AppPalette.blue,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = AppPalette.text,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "📍 ${p.location}",
                                        color = AppPalette.muted,
                                        fontSize = 12.sp
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingPlantation = p },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = AppPalette.muted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { itemToDelete = p },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // METRICS ROW
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBadge(label = "Variety", value = p.variety, modifier = Modifier.weight(1.2f))
                                MetricBadge(label = "Age", value = "${p.ageYears} yrs", modifier = Modifier.weight(0.8f))
                                MetricBadge(label = "Area", value = "${p.areaAcres} ac", modifier = Modifier.weight(0.8f))
                                MetricBadge(label = "Trees", value = "${p.treeCount}", modifier = Modifier.weight(0.8f))
                            }

                            if (p.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notes: ${p.notes}",
                                    color = AppPalette.muted.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!isActive) {
                                Button(
                                    onClick = {
                                        activePlantation = p
                                        manager.setActivePlantation(p)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Set As Active Estate", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD / EDIT DIALOG
    if (showAddDialog || editingPlantation != null) {
        val target = editingPlantation
        PlantationFormDialog(
            initial = target,
            onDismiss = {
                showAddDialog = false
                editingPlantation = null
            },
            onSave = { updated ->
                if (target != null) {
                    manager.updatePlantation(updated) {
                        manager.getPlantations { plantations = it }
                    }
                } else {
                    manager.addPlantation(updated) {
                        manager.getPlantations { plantations = it }
                    }
                }
                showAddDialog = false
                editingPlantation = null
            }
        )
    }

    // DELETE CONFIRMATION
    itemToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Plantation?", color = AppPalette.text, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${p.name}\"? Associated records will be unlinked.", color = AppPalette.muted) },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deletePlantation(p.id) {
                            manager.getPlantations { plantations = it }
                        }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = AppPalette.muted)
                }
            },
            containerColor = AppPalette.surface
        )
    }
}

@Composable
fun MetricBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = AppPalette.background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
            Text(label, color = AppPalette.accent, fontSize = 9.sp)
            Text(
                value,
                color = AppPalette.text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlantationFormDialog(
    initial: Plantation?,
    onDismiss: () -> Unit,
    onSave: (Plantation) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var variety by remember { mutableStateOf(initial?.variety ?: "Arabica (Selection 795)") }
    var ageYearsText by remember { mutableStateOf(initial?.ageYears?.toString() ?: "5") }
    var areaAcresText by remember { mutableStateOf(initial?.areaAcres?.toString() ?: "5.0") }
    var treeCountText by remember { mutableStateOf(initial?.treeCount?.toString() ?: "2000") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val varieties = listOf(
        "Arabica (Selection 795)",
        "Arabica (Cauvery / Catimor)",
        "Arabica (Chandragiri)",
        "Arabica (SLN 9)",
        "Robusta (S.274)",
        "Robusta (Congensis x Robusta / C x R)",
        "Arabica (Kent / Bourbon)"
    )

    var expandedVariety by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial != null) "Edit Plantation" else "Add New Plantation",
                color = AppPalette.text,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Estate / Block Name") },
                    placeholder = { Text("e.g. Silver Peak Estate Block B") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Region") },
                    placeholder = { Text("e.g. Chikkamagaluru, Karnataka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())

                // VARIETY SELECTOR
                Box {
                    OutlinedTextField(
                        value = variety,
                        onValueChange = {},
                        label = { Text("Coffee Variety") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedVariety = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedVariety = true }
                    , colors = AppOutlinedTextFieldColors())
                    DropdownMenu(
                        expanded = expandedVariety,
                        onDismissRequest = { expandedVariety = false }
                    ) {
                        varieties.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v) },
                                onClick = {
                                    variety = v
                                    expandedVariety = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageYearsText,
                        onValueChange = { ageYearsText = it },
                        label = { Text("Age (Yrs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    , colors = AppOutlinedTextFieldColors())
                    OutlinedTextField(
                        value = areaAcresText,
                        onValueChange = { areaAcresText = it },
                        label = { Text("Area (Acres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    , colors = AppOutlinedTextFieldColors())
                }

                OutlinedTextField(
                    value = treeCountText,
                    onValueChange = { treeCountText = it },
                    label = { Text("Estimated Trees") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Plant Notes / Shade Trees") },
                    placeholder = { Text("e.g. Silver oak canopy, soil tested acidic...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                , colors = AppOutlinedTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val result = Plantation(
                            id = initial?.id ?: "",
                            name = name.trim(),
                            location = location.trim(),
                            variety = variety,
                            ageYears = ageYearsText.toIntOrNull() ?: 5,
                            areaAcres = areaAcresText.toDoubleOrNull() ?: 5.0,
                            treeCount = treeCountText.toIntOrNull() ?: 2000,
                            notes = notes.trim(),
                            createdAt = initial?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(result)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt)
            ) {
                Text("Save Estate")
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
