package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.agriplus.R
import com.example.agriplus.data.*
import com.example.agriplus.data.plantation.PlantationManager
import com.example.agriplus.data.weather.*
import com.example.agriplus.utils.ApiClient
import com.example.agriplus.utils.ImageQualityChecker
import com.example.agriplus.utils.PdfReportGenerator
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val manager = remember { RealtimeManager() }
    val plantationManager = remember { PlantationManager(context) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var diseaseInfo by remember { mutableStateOf<DiseaseInfo?>(null) }
    var confidence by remember { mutableStateOf(0.0) }
    var lastScannedHistoryItem by remember { mutableStateOf<HistoryItem?>(null) }

    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var qualityWarning by remember { mutableStateOf<String?>(null) }

    // Recent hashes for duplicate detection
    val recentHashes = remember { mutableStateListOf<Long>() }

    // Live weather snapshot
    var homeWeather by remember { mutableStateOf<WeatherData?>(null) }

    LaunchedEffect(Unit) {
        WeatherRepository.fetchWeather(WeatherRepository.getDefaultLocation()) { result ->
            result.onSuccess { data -> homeWeather = data }
        }
    }

    val plantAnim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.plant))
    val scanAnim by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan))

    val plantProgress by animateLottieCompositionAsState(plantAnim, iterations = LottieConstants.IterateForever)
    val scanProgress by animateLottieCompositionAsState(scanAnim, iterations = LottieConstants.IterateForever)

    fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    fun onImageSelected(bmp: Bitmap) {
        bitmap = bmp
        imageUri = null
        diseaseInfo = null
        errorMessage = ""

        // Smart Quality Pre-Check
        val eval = ImageQualityChecker.evaluate(bmp, recentHashes)
        qualityWarning = eval.warning
        if (eval.imageHash != 0L && !recentHashes.contains(eval.imageHash)) {
            recentHashes.add(eval.imageHash)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bmp?.let { onImageSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            errorMessage = "Camera permission denied"
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            val bmp = getBitmapFromUri(it)
            onImageSelected(bmp)
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun startScan() {
        val bmp = bitmap ?: run {
            errorMessage = "Please capture or upload a leaf image first"
            return
        }

        isScanning = true
        errorMessage = ""
        diseaseInfo = null
        confidence = 0.0

        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        ApiClient.uploadImage(stream.toByteArray()) { response ->
            isScanning = false
            try {
                val obj = JSONObject(response)
                    .getJSONArray("predictions")
                    .getJSONObject(0)

                val label = obj.getString("class")
                val conf = obj.getDouble("confidence")

                if (conf < 0.60) {
                    errorMessage = "❌ Not a valid leaf image. Please photograph a clear coffee leaf."
                    diseaseInfo = null
                    return@uploadImage
                }

                val info = DiseaseRepository.getInfo(label)
                diseaseInfo = info
                confidence = conf

                val activeEstate = plantationManager.getActivePlantation()
                val historyItem = HistoryItem(
                    result = label,
                    confidence = conf,
                    imageBase64 = bitmapToBase64(bmp),
                    timestamp = System.currentTimeMillis()
                )
                lastScannedHistoryItem = historyItem
                manager.saveResult(historyItem)

            } catch (e: Exception) {
                errorMessage = "Network or inference error. Please verify connection and retry."
            }
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
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // 1. TOP HEADER (USER & APP BRANDING)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { navController.navigate("profile") }
                ) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppPalette.background)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("AgriPlus", color = AppPalette.text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = AppPalette.surfaceAlt.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Pro",
                                    color = AppPalette.accentText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Hello, ${user?.displayName?.split(" ")?.firstOrNull() ?: "Planter"} 👋",
                            color = AppPalette.muted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigate("consultation") }) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Expert Desk", tint = AppPalette.accent)
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = AppPalette.muted)
                    }
                }
            }

            // 2. WEATHER & COFFEE ADVISORY PILL
            homeWeather?.let { w ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("weather") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(CoffeeAdvisoryEngine.getWeatherIconEmoji(w.current.weatherCode), fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${w.location.name}: ${w.current.temperature.toInt()}°C • ${w.current.conditionText}",
                                    color = AppPalette.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "💧 ${w.current.humidity}% RH • ${w.advisory.rustRiskLevel}",
                                    color = if (w.advisory.rustRiskLevel.contains("High", true)) AppPalette.danger else AppPalette.accentText,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (w.advisory.isSprayingFavorable) AppPalette.surfaceAlt.copy(alpha = 0.4f) else AppPalette.dangerContainer
                        ) {
                            Text(
                                text = if (w.advisory.isSprayingFavorable) "Spray OK ✓" else "Rain Alert ✗",
                                color = if (w.advisory.isSprayingFavorable) AppPalette.accentText else AppPalette.danger,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 3. MAIN DASHBOARD 6 SECTIONS (MATCHING USER PROPOSED STRUCTURE)
            Text(
                text = "Farm Management Hub",
                color = AppPalette.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            // 6-CARD GRID (3 ROWS OF 2 TILES)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // ROW 1: AI Scanner & Dashboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "AI Scanner",
                        subtitle = "Instant leaf diagnosis",
                        icon = Icons.Default.QrCodeScanner,
                        modifier = Modifier.weight(1f),
                        isHighlighted = true,
                        onClick = { /* Already on scanner */ }
                    )
                    DashboardTile(
                        title = "Dashboard",
                        subtitle = "Health analytics",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("dashboard") }
                    )
                }

                // ROW 2: My Plantation & Treatment Guide
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "My Plantation",
                        subtitle = "Estate & plant tracking",
                        icon = Icons.Default.Yard,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("plantation") }
                    )
                    DashboardTile(
                        title = "Treatment Guide",
                        subtitle = "Offline disease library",
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("treatment_guide") }
                    )
                }

                // ROW 3: Reminders & Scan History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "Reminders",
                        subtitle = "Treatment schedule",
                        icon = Icons.Default.Alarm,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("reminders") }
                    )
                    DashboardTile(
                        title = "Scan History",
                        subtitle = "Comparison & reports",
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("history") }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // 4. SCANNER ACTIVE WORKSPACE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📷 Coffee Leaf Inspection Camera",
                            color = AppPalette.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Roboflow Core Model",
                            color = AppPalette.accent,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // IMAGE / LOTTIE VIEWPORT
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppPalette.background),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            bitmap != null -> {
                                Image(
                                    bitmap = bitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            imageUri != null -> {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                LottieAnimation(plantAnim, { plantProgress }, Modifier.size(170.dp))
                            }
                        }
                    }

                    // QUALITY WARNING BANNER
                    qualityWarning?.let { warning ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFE65100).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = warning,
                                color = Color(0xFFFFB74D),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // CAPTURE / UPLOAD CONTROLS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPalette.text)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Camera", fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { startScan() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isScanning && bitmap != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppPalette.surfaceAlt,
                            disabledContainerColor = AppPalette.background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Biotech, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isScanning) "Diagnosing Leaf..." else "Scan Leaf Condition", fontWeight = FontWeight.Bold)
                    }

                    AnimatedVisibility(isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(scanAnim, { scanProgress }, Modifier.size(90.dp))
                        }
                    }

                    // ERROR CARD
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFB71C1C).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(errorMessage, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }

            // 5. SCAN RESULT CARD WITH PDF & ACTIONS
            AnimatedVisibility(visible = diseaseInfo != null) {
                val info = diseaseInfo ?: return@AnimatedVisibility
                val isHealthy = info.name.contains("healthy", true)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHealthy) AppPalette.surfaceAlt else AppPalette.dangerContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isHealthy) AppPalette.line else AppPalette.danger.copy(alpha = 0.72f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                info.name,
                                color = if (isHealthy) AppPalette.text else AppPalette.danger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 24.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isHealthy) AppPalette.surface else AppPalette.danger
                            ) {
                                Text(
                                    "Confidence ${(confidence * 100).toInt()}%",
                                    color = if (isHealthy) AppPalette.muted else Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { confidence.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isHealthy) AppPalette.accent else AppPalette.danger,
                            trackColor = AppPalette.text.copy(alpha = 0.2f)
                        )

                        Spacer(Modifier.height(12.dp))
                        Text("Cause", color = AppPalette.muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(info.cause, color = AppPalette.text, fontSize = 14.sp, lineHeight = 20.sp)

                        Spacer(Modifier.height(6.dp))
                        Text("Symptoms", color = AppPalette.muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(info.symptoms, color = AppPalette.text, fontSize = 14.sp, lineHeight = 20.sp)

                        Spacer(Modifier.height(6.dp))
                        Text("Recommended treatment", color = AppPalette.muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(info.treatment, color = AppPalette.text, fontSize = 14.sp, lineHeight = 20.sp)

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = AppPalette.text.copy(alpha = 0.15f))
                        Spacer(Modifier.height(10.dp))

                        // PDF & QUICK ACTIONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    lastScannedHistoryItem?.let { item ->
                                        val pdf = PdfReportGenerator.generateScanReportPdf(context, item)
                                        if (pdf != null) {
                                            PdfReportGenerator.viewPdfReport(context, pdf)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPalette.text)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PDF Report", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { navController.navigate("reminders") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AppPalette.surfaceAlt),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Set Reminder", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DashboardTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(95.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) AppPalette.surfaceAlt else AppPalette.background
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isHighlighted) AppPalette.blue else AppPalette.line)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppPalette.accent,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    color = AppPalette.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = AppPalette.muted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
