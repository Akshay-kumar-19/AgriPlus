package com.example.agriplus.data.weather

import java.text.SimpleDateFormat
import java.util.*

object CoffeeAdvisoryEngine {

    /**
     * Map WMO weather code to user-friendly description and icon
     */
    fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy / Mist"
            51 -> "Light Drizzle"
            53 -> "Moderate Drizzle"
            55 -> "Dense Drizzle"
            56, 57 -> "Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66, 67 -> "Freezing Rain"
            71 -> "Slight Snow"
            73 -> "Moderate Snow"
            75 -> "Heavy Snow"
            77 -> "Snow Grains"
            80 -> "Slight Rain Showers"
            81 -> "Moderate Rain Showers"
            82 -> "Violent Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Scattered Weather"
        }
    }

    fun getWeatherIconEmoji(code: Int): String {
        return when (code) {
            0 -> "☀️"
            1, 2 -> "🌤️"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌦️"
            61, 63 -> "🌧️"
            65, 82 -> "⛈️"
            80, 81 -> "🌦️"
            95, 96, 99 -> "🌩️"
            else -> "🌤️"
        }
    }

    fun formatDayName(dateString: String, index: Int): String {
        if (index == 0) return "Today"
        if (index == 1) return "Tomorrow"
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateString)
            val dayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            date?.let { dayFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Generate comprehensive agronomic advisory tailored for coffee plants
     */
    fun generateAdvisory(
        current: CurrentWeather,
        daily: List<DailyForecastDay>,
        location: LocationPreset
    ): CoffeeAdvisory {
        val temp = current.temperature
        val humidity = current.humidity
        val rain = current.precipitation
        val wind = current.windSpeed
        val next24hRainProb = daily.firstOrNull()?.precipitationProbability ?: 0
        val next24hRainSum = daily.firstOrNull()?.precipitationSum ?: rain

        val tips = mutableListOf<String>()

        // 1. Rust and Fungal Disease Risk Assessment
        val (rustLevel, rustDetail) = when {
            humidity >= 80 && (temp in 20.0..28.0) -> {
                Pair(
                    "High Fungal Risk ⚠️",
                    "Relative humidity is high ($humidity%) with warm temperatures (${temp.toInt()}°C). Ideal breeding conditions for Coffee Leaf Rust (Hemileia vastatrix) and Phoma. Inspect leaf undersides for powdery orange pustules and consider prophylactic 0.5% Bordeaux spray or systemic fungicide."
                )
            }
            humidity >= 75 || rain > 2.0 -> {
                Pair(
                    "Moderate Risk ⚠️",
                    "Elevated humidity ($humidity%) and lingering surface moisture may promote fungal spore development. Keep estate shaded canopies balanced to maximize aeration."
                )
            }
            temp > 30.0 -> {
                Pair(
                    "Low Rust / High Pest Risk 🐛",
                    "Dry and warm conditions reduce fungal propagation but elevate risk of Red Spider Mite and Green Scale infestations."
                )
            }
            else -> {
                Pair(
                    "Low Disease Risk 🌿",
                    "Current humidity and temperature are within safe margins for fungal outbreaks. Continue standard cultural hygiene."
                )
            }
        }

        // 2. Spraying Window Suitability
        val isSprayingFavorable = rain == 0.0 && next24hRainProb < 35 && wind < 18.0
        val sprayingAdvice = when {
            rain > 0.5 || next24hRainProb >= 50 -> {
                "Not Recommended: Rain occurring or expected (${next24hRainProb}% chance). Foliar fertilizers or fungicides will be washed off before absorption. Postpone spraying."
            }
            wind >= 18.0 -> {
                "Avoid Spraying: High wind speeds (${wind.toInt()} km/h) cause chemical spray drift and poor canopy coverage. Wait for calmer morning hours."
            }
            else -> {
                "Favorable Spray Window: Dry foliage and light wind conditions allow optimal foliar nutrient and fungicide absorption. Best time is early morning or late afternoon."
            }
        }

        // 3. Irrigation Recommendation
        val irrigationAdvice = when {
            rain >= 15.0 || next24hRainSum >= 15.0 -> {
                "No Irrigation Required: Ample natural precipitation (${String.format(Locale.US, "%.1f", next24hRainSum)} mm). Ensure estate cradle pits and water drainage channels are clear to prevent waterlogging."
            }
            rain in 2.0..14.9 -> {
                "Moisture Adequate: Light to moderate precipitation keeps root zone moist. Pause artificial drip/sprinklers."
            }
            temp >= 28.0 && humidity < 50 -> {
                "Irrigation Needed: High temperature combined with low humidity is accelerating evapotranspiration. Provide supplementary drip or overhead sprinkler irrigation."
            }
            else -> {
                "Moderate Moisture: Check topsoil around tree drip lines. Maintain organic leaf mulch to conserve sub-surface moisture."
            }
        }

        // 4. Temperature Impact
        val temperatureImpact = when {
            temp in 18.0..24.0 -> {
                "Ideal Arabica growth range (${temp.toInt()}°C). Facilitates steady vegetative flushes and optimum photosynthesis."
            }
            temp in 24.1..29.0 -> {
                "Well suited for Robusta coffee. Arabica trees may experience mild midday transpiration stress."
            }
            temp > 29.0 -> {
                "High heat stress alert (${temp.toInt()}°C). Conserve two-tier shade trees (Silver Oak / Dadap) to prevent leaf scorching."
            }
            else -> {
                "Cool conditions (${temp.toInt()}°C). Plant vegetative development slows down. Avoid heavy root-fertilizer doses until ground warms."
            }
        }

        // 5. Humidity Impact
        val humidityImpact = when {
            humidity > 80 -> "High atmospheric moisture ($humidity%). Increases risk of Leaf Rust, Black Rot (Koleroga), and Anthracnose."
            humidity in 60..80 -> "Optimal humidity ($humidity%). Perfect for coffee canopy respiration and stomatal opening."
            else -> "Low humidity ($humidity%). Monitor young seedlings for moisture stress and leaf tip drying."
        }

        // 6. Rainfall Impact
        val rainfallImpact = when {
            next24hRainSum >= 25.0 -> "Heavy rainfall ($next24hRainSum mm). Clear estate drains to prevent root rot and premature berry drop."
            next24hRainSum in 5.0..24.9 -> "Beneficial showers ($next24hRainSum mm). Excellent for nutrient dissolution and berry expansion."
            else -> "Dry period ($next24hRainSum mm). Good condition for harvest drying yard operations and weed clearing."
        }

        // Actionable Tips compilation
        if (humidity >= 80) {
            tips.add("Prune dense secondary branches (desuckering) to boost airflow inside coffee bushes.")
            tips.add("Apply prophylactic 0.5% Neutral Bordeaux Mixture or Hexaconazole for Leaf Rust prevention.")
        }
        if (isSprayingFavorable) {
            tips.add("Take advantage of the calm weather to apply micronutrient foliar spray (Zinc + Boron + Urea).")
        }
        if (rain > 10.0) {
            tips.add("Inspect low-lying blocks for standing water to prevent wet root rot (Rosellinia).")
        } else if (temp > 28.0) {
            tips.add("Spread dry leaf or compost mulch around the basin to retain soil moisture.")
        }
        tips.add("Monitor for Coffee Berry Borer (CBB) in ripening berries; install Brocap pheromone traps if necessary.")
        tips.add("Inspect newly planted shade trees and regulate shade intensity to 50-60% sunlight penetration.")

        // Determine Overall Severity
        val severity = when {
            humidity >= 82 && temp in 21.0..27.0 -> AdvisorySeverity.HIGH_RISK
            next24hRainSum >= 30.0 || temp >= 32.0 -> AdvisorySeverity.CAUTION
            humidity in 60..80 && temp in 18.0..25.0 -> AdvisorySeverity.OPTIMAL
            else -> AdvisorySeverity.FAVORABLE
        }

        val headline = when (severity) {
            AdvisorySeverity.HIGH_RISK -> "High Leaf Rust Risk • Urgent Canopy Care"
            AdvisorySeverity.CAUTION -> "Extreme Weather Watch • Active Monitoring"
            AdvisorySeverity.OPTIMAL -> "Optimal Agro-Climatic Conditions"
            AdvisorySeverity.FAVORABLE -> "Stable Coffee Growing Weather"
        }

        val summary = "Current conditions in ${location.name} (${temp.toInt()}°C, $humidity% RH, ${String.format(Locale.US, "%.1f", rain)}mm rain) present $headline. Follow the advisory below to protect yield and tree vigor."

        return CoffeeAdvisory(
            severity = severity,
            statusHeadline = headline,
            summary = summary,
            rustRiskLevel = rustLevel,
            rustRiskDetail = rustDetail,
            isSprayingFavorable = isSprayingFavorable,
            sprayingAdvice = sprayingAdvice,
            irrigationAdvice = irrigationAdvice,
            temperatureImpact = temperatureImpact,
            humidityImpact = humidityImpact,
            rainfallImpact = rainfallImpact,
            actionableTips = tips
        )
    }
}
