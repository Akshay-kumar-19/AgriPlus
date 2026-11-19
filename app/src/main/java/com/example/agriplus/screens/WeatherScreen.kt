package com.example.agriplus.screens

import com.example.agriplus.ui.theme.AppPalette
import com.example.agriplus.ui.theme.AppOutlinedTextFieldColors

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agriplus.data.weather.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(navController: NavController) {

    val context = LocalContext.current

    var selectedLocation by remember { mutableStateOf(WeatherRepository.getDefaultLocation()) }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showLocationDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<LocationPreset>>(WeatherRepository.COFFEE_REGIONS) }
    var isSearching by remember { mutableStateOf(false) }

    fun loadWeather(location: LocationPreset) {
        isLoading = true
        errorMessage = null
        selectedLocation = location
        WeatherRepository.fetchWeather(location) { result ->
            isLoading = false
            result.onSuccess { data ->
                weatherData = data
            }.onFailure { err ->
                errorMessage = err.message ?: "Failed to load weather data"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadWeather(selectedLocation)
    }

    // Location Permission Launcher for GPS detection
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastKnown: Location? = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (lastKnown != null) {
                    val lat = lastKnown.latitude
                    val lon = lastKnown.longitude
                    var detectedName = "My Plantation Location"
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                            detectedName = locality ?: "Current Location"
                        }
                    } catch (e: Exception) {
                        // ignore geocoder fail
                    }

                    val gpsLocation = LocationPreset(
                        name = detectedName,
                        region = "GPS Coordinates (${String.format(Locale.US, "%.2f", lat)}, ${String.format(Locale.US, "%.2f", lon)})",
                        latitude = lat,
                        longitude = lon,
                        coffeeType = "Local Farm Zone",
                        isPreset = false
                    )
                    loadWeather(gpsLocation)
                    showLocationDialog = false
                }
            } catch (e: SecurityException) {
                // permission revoked
            }
        }
    }

    // Handle search text changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            isSearching = true
            WeatherRepository.searchLocations(searchQuery) { results ->
                searchResults = results
                isSearching = false
            }
        } else {
            searchResults = WeatherRepository.COFFEE_REGIONS
            isSearching = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppPalette.background, // Deep navy
                        AppPalette.background, // Slate midnight
                        AppPalette.background  // Deep forest coffee tint
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // TOP APP BAR
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Weather & Plant Care",
                            color = AppPalette.text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Agro-Meteorological Advisory",
                            color = AppPalette.accent,
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
                    val infiniteTransition = rememberInfiniteTransition(label = "spin")
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "spin"
                    )

                    IconButton(
                        onClick = { loadWeather(selectedLocation) },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = AppPalette.text,
                            modifier = if (isLoading) Modifier.rotate(angle) else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (isLoading && weatherData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppPalette.blue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching live satellite weather data...",
                            color = AppPalette.text.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (errorMessage != null && weatherData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = errorMessage ?: "Unable to fetch weather",
                                color = AppPalette.text,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { loadWeather(selectedLocation) },
                                colors = ButtonDefaults.buttonColors(containerColor = AppPalette.blue)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            } else {
                weatherData?.let { data ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {

                        // 1. LOCATION SELECTOR CARD
                        item {
                            LocationSelectorCard(
                                location = data.location,
                                onChangeClick = { showLocationDialog = true }
                            )
                        }

                        // 2. CURRENT WEATHER HERO CARD
                        item {
                            CurrentWeatherHeroCard(
                                current = data.current,
                                todayForecast = data.daily.firstOrNull()
                            )
                        }

                        // 3. 4-GRID KEY METRICS (Temp, Humidity, Rain, Wind)
                        item {
                            KeyMetricsRow(current = data.current, today = data.daily.firstOrNull())
                        }

                        // 4. GENERAL COFFEE PLANT-CARE ADVISORY (THE CORE FEATURE)
                        item {
                            CoffeeAdvisoryCard(advisory = data.advisory)
                        }

                        // 5. SPRAYING & IRRIGATION DECISION TILES
                        item {
                            DecisionSupportCards(advisory = data.advisory)
                        }

                        // 6. ACTIONABLE PLANT-CARE CHECKLIST
                        item {
                            ActionableTipsCard(advisory = data.advisory)
                        }

                        // 7. 7-DAY WEATHER & RAINFALL FORECAST
                        item {
                            ForecastSection(dailyList = data.daily)
                        }
                    }
                }
            }
        }
    }

    // LOCATION SELECTION DIALOG / BOTTOM SHEET
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Close", color = AppPalette.accent)
                }
            },
            title = {
                Text(
                    text = "Select Coffee Estate / Region",
                    fontWeight = FontWeight.Bold,
                    color = AppPalette.text,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search city or estate...", color = AppPalette.muted) },
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
                        colors = AppOutlinedTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // GPS Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = AppPalette.accent
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Use Device GPS Location",
                                    color = AppPalette.text,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Detect microclimate from current coordinates",
                                    color = AppPalette.muted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (searchQuery.isEmpty()) "Famous Coffee Growing Zones:" else "Results:",
                        color = AppPalette.muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = AppPalette.blue
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(searchResults) { loc ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showLocationDialog = false
                                            loadWeather(loc)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedLocation.name == loc.name)
                                            AppPalette.surfaceAlt
                                        else
                                            AppPalette.background
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                loc.name,
                                                color = AppPalette.text,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (loc.isPreset) {
                                                Surface(
                                                    color = AppPalette.surfaceAlt.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "Coffee Zone",
                                                        color = AppPalette.accentText,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            loc.region,
                                            color = AppPalette.muted,
                                            fontSize = 11.sp
                                        )
                                        if (loc.coffeeType.isNotEmpty()) {
                                            Text(
                                                loc.coffeeType,
                                                color = AppPalette.accent,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = AppPalette.surface
        )
    }
}

@Composable
fun LocationSelectorCard(
    location: LocationPreset,
    onChangeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChangeClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface.copy(alpha = 0.9f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppPalette.surfaceAlt.copy(alpha = 0.3f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AppPalette.accent
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = location.name,
                            color = AppPalette.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (location.elevationMeters > 0) {
                            Text(
                                text = "• ${location.elevationMeters}m MSL",
                                color = AppPalette.muted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = location.region,
                        color = AppPalette.muted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppPalette.surface,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change",
                        color = AppPalette.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppPalette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherHeroCard(
    current: CurrentWeather,
    todayForecast: DailyForecastDay?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppPalette.surface.copy(alpha = 0.95f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppPalette.surface.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "${current.temperature.toInt()}°C",
                            color = AppPalette.text,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 56.sp
                        )
                        Text(
                            text = "Feels like ${current.apparentTemperature.toInt()}°C",
                            color = AppPalette.muted,
                            fontSize = 14.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CoffeeAdvisoryEngine.getWeatherIconEmoji(current.weatherCode),
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = current.conditionText,
                            color = AppPalette.accentText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppPalette.text.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                // Max/Min today & Day indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (todayForecast != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "High: ${todayForecast.maxTemp.toInt()}°C  •  Low: ${todayForecast.minTemp.toInt()}°C",
                                color = AppPalette.text.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (current.isDay) Color(0xFFFBC02D).copy(alpha = 0.2f) else AppPalette.blue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (current.isDay) "☀️ Daylight" else "🌙 Night",
                            color = if (current.isDay) Color(0xFFFFEE58) else AppPalette.blue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyMetricsRow(
    current: CurrentWeather,
    today: DailyForecastDay?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.WaterDrop,
            iconTint = AppPalette.blue,
            label = "Humidity",
            value = "${current.humidity}%",
            subLabel = if (current.humidity >= 80) "Rust Alert" else "Normal"
        )
        MetricTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Umbrella,
            iconTint = AppPalette.blue,
            label = "Rainfall",
            value = "${String.format(Locale.US, "%.1f", current.precipitation)} mm",
            subLabel = if (today != null) "${today.precipitationProbability}% chance" else "Today"
        )
        MetricTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Air,
            iconTint = Color(0xFF80CBC4),
            label = "Wind",
            value = "${current.windSpeed.toInt()} km/h",
            subLabel = if (current.windSpeed >= 18) "Gusty" else "Calm"
        )
    }
}

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subLabel: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = AppPalette.text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = label,
                color = AppPalette.muted,
                fontSize = 11.sp
            )
            Text(
                text = subLabel,
                color = iconTint,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CoffeeAdvisoryCard(advisory: CoffeeAdvisory) {

    val bannerColor = when (advisory.severity) {
        AdvisorySeverity.HIGH_RISK -> AppPalette.danger
        AdvisorySeverity.CAUTION -> AppPalette.warning
        AdvisorySeverity.OPTIMAL -> AppPalette.surfaceAlt
        AdvisorySeverity.FAVORABLE -> AppPalette.surfaceAlt
    }

    val bannerBg = when (advisory.severity) {
        AdvisorySeverity.HIGH_RISK -> AppPalette.dangerContainer
        AdvisorySeverity.CAUTION -> AppPalette.surfaceAlt
        AdvisorySeverity.OPTIMAL -> AppPalette.background
        AdvisorySeverity.FAVORABLE -> AppPalette.background
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (advisory.severity == AdvisorySeverity.HIGH_RISK) AppPalette.danger.copy(alpha = 0.72f) else AppPalette.line
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "☕ Coffee Agronomy Advisory",
                        color = AppPalette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    color = bannerColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = advisory.severity.name,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = advisory.statusHeadline,
                color = if (advisory.severity == AdvisorySeverity.HIGH_RISK) AppPalette.danger else AppPalette.accentText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = advisory.summary,
                color = AppPalette.text.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = AppPalette.text.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Coffee Leaf Rust Section
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Coronavirus,
                    contentDescription = null,
                    tint = if (advisory.rustRiskLevel.contains("High", true)) AppPalette.danger else AppPalette.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Leaf Rust & Fungal Threat: ${advisory.rustRiskLevel}",
                        color = if (advisory.rustRiskLevel.contains("High", ignoreCase = true)) AppPalette.danger else AppPalette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = advisory.rustRiskDetail,
                        color = AppPalette.muted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DecisionSupportCards(advisory: CoffeeAdvisory) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Spraying Window Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (advisory.isSprayingFavorable) AppPalette.surfaceAlt else AppPalette.dangerContainer
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (advisory.isSprayingFavorable) AppPalette.line else AppPalette.danger.copy(alpha = 0.65f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (advisory.isSprayingFavorable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (advisory.isSprayingFavorable) AppPalette.blue else AppPalette.danger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Spray Window",
                        color = AppPalette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = advisory.sprayingAdvice,
                    color = AppPalette.muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Irrigation Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppPalette.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = null,
                        tint = AppPalette.blue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Irrigation",
                        color = AppPalette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = advisory.irrigationAdvice,
                    color = AppPalette.muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ActionableTipsCard(advisory: CoffeeAdvisory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = AppPalette.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Farm Management Actions for Today",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            advisory.actionableTips.forEach { tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = AppPalette.blue,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        color = AppPalette.text.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = AppPalette.text.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            // Microclimate Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🌱 Temperature: ${advisory.temperatureImpact}",
                    color = AppPalette.muted,
                    fontSize = 11.sp
                )
                Text(
                    text = "💧 Humidity: ${advisory.humidityImpact}",
                    color = AppPalette.muted,
                    fontSize = 11.sp
                )
                Text(
                    text = "🌧️ Rainfall: ${advisory.rainfallImpact}",
                    color = AppPalette.muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ForecastSection(dailyList: List<DailyForecastDay>) {
    Column {
        Text(
            text = "7-Day Weather & Rainfall Forecast",
            color = AppPalette.text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppPalette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppPalette.line)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                dailyList.forEachIndexed { index, day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Day name
                        Column(modifier = Modifier.width(90.dp)) {
                            Text(
                                text = day.dayName,
                                color = AppPalette.text,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Text(
                                text = day.conditionText,
                                color = AppPalette.muted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Weather icon emoji
                        Text(
                            text = CoffeeAdvisoryEngine.getWeatherIconEmoji(day.weatherCode),
                            fontSize = 20.sp
                        )

                        // Rain probability
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(70.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = AppPalette.blue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${day.precipitationProbability}%",
                                color = AppPalette.blue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (day.precipitationSum > 0.0) {
                                Text(
                                    text = " (${day.precipitationSum.toInt()}mm)",
                                    color = AppPalette.muted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Temperature min/max
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${day.maxTemp.toInt()}°",
                                color = AppPalette.text,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = " / ${day.minTemp.toInt()}°",
                                color = AppPalette.muted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (index < dailyList.size - 1) {
                        HorizontalDivider(color = AppPalette.text.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}
