package com.example.agriplus.data.weather

/**
 * Data models for Weather and Coffee Agronomy Advisory
 */

enum class AdvisorySeverity {
    OPTIMAL,
    FAVORABLE,
    CAUTION,
    HIGH_RISK
}

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val precipitation: Double,
    val rain: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val isDay: Boolean,
    val conditionText: String
)

data class DailyForecastDay(
    val date: String,
    val dayName: String,
    val weatherCode: Int,
    val conditionText: String,
    val maxTemp: Double,
    val minTemp: Double,
    val precipitationSum: Double,
    val precipitationProbability: Int
)

data class CoffeeAdvisory(
    val severity: AdvisorySeverity,
    val statusHeadline: String,
    val summary: String,
    val rustRiskLevel: String,
    val rustRiskDetail: String,
    val isSprayingFavorable: Boolean,
    val sprayingAdvice: String,
    val irrigationAdvice: String,
    val temperatureImpact: String,
    val humidityImpact: String,
    val rainfallImpact: String,
    val actionableTips: List<String>
)

data class LocationPreset(
    val name: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int = 1000,
    val coffeeType: String = "Arabica & Robusta",
    val isPreset: Boolean = true
)

data class WeatherData(
    val location: LocationPreset,
    val current: CurrentWeather,
    val daily: List<DailyForecastDay>,
    val advisory: CoffeeAdvisory,
    val fetchedAtMillis: Long = System.currentTimeMillis()
)
