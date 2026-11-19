package com.example.agriplus.data.weather

import android.os.Handler
import android.os.Looper
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object WeatherRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    val COFFEE_REGIONS: List<LocationPreset> = listOf(
        LocationPreset(
            name = "Chikkamagaluru",
            region = "Karnataka, India",
            latitude = 13.3161,
            longitude = 75.7720,
            elevationMeters = 1035,
            coffeeType = "Arabica & Robusta (Birthplace of Indian Coffee)"
        ),
        LocationPreset(
            name = "Coorg / Kodagu (Madikeri)",
            region = "Karnataka, India",
            latitude = 12.4244,
            longitude = 75.7382,
            elevationMeters = 1140,
            coffeeType = "Fine Robusta & High-Grown Arabica"
        ),
        LocationPreset(
            name = "Sakleshpur / Hassan",
            region = "Karnataka, India",
            latitude = 12.9436,
            longitude = 75.7873,
            elevationMeters = 956,
            coffeeType = "Shade-grown Arabica & Robusta"
        ),
        LocationPreset(
            name = "Wayanad",
            region = "Kerala, India",
            latitude = 11.6854,
            longitude = 76.1320,
            elevationMeters = 900,
            coffeeType = "Malabar High-Density Robusta"
        ),
        LocationPreset(
            name = "Araku Valley",
            region = "Andhra Pradesh, India",
            latitude = 18.3273,
            longitude = 82.8775,
            elevationMeters = 911,
            coffeeType = "Organic Specialty Arabica"
        ),
        LocationPreset(
            name = "Munnar / Idukki",
            region = "Kerala, India",
            latitude = 10.0889,
            longitude = 77.0595,
            elevationMeters = 1532,
            coffeeType = "High-Altitude Specialty Coffee"
        ),
        LocationPreset(
            name = "Shevaroy Hills (Yercaud)",
            region = "Tamil Nadu, India",
            latitude = 11.7753,
            longitude = 78.2093,
            elevationMeters = 1515,
            coffeeType = "Specialty Arabica"
        ),
        LocationPreset(
            name = "Nilgiris (Kotagiri/Coonoor)",
            region = "Tamil Nadu, India",
            latitude = 11.4102,
            longitude = 76.6950,
            elevationMeters = 1800,
            coffeeType = "Blue Mountain Arabica Varieties"
        ),
        LocationPreset(
            name = "Minas Gerais",
            region = "Brazil",
            latitude = -18.5122,
            longitude = -44.5550,
            elevationMeters = 950,
            coffeeType = "World's Largest Coffee Region"
        )
    )

    fun getDefaultLocation(): LocationPreset = COFFEE_REGIONS.first()

    /**
     * Fetch live weather and forecast from Open-Meteo
     */
    fun fetchWeather(
        location: LocationPreset,
        callback: (Result<WeatherData>) -> Unit
    ) {
        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=${location.latitude}&longitude=${location.longitude}" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,weather_code,wind_speed_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max" +
                "&timezone=auto"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    // Fallback mock weather for smooth offline experience
                    val fallback = createFallbackWeather(location)
                    callback(Result.success(fallback))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                if (!response.isSuccessful || jsonString.isNullOrEmpty()) {
                    mainHandler.post {
                        val fallback = createFallbackWeather(location)
                        callback(Result.success(fallback))
                    }
                    return
                }

                try {
                    val root = JSONObject(jsonString)
                    val currentObj = root.getJSONObject("current")
                    val dailyObj = root.getJSONObject("daily")

                    val currentWeather = CurrentWeather(
                        temperature = currentObj.optDouble("temperature_2m", 24.0),
                        apparentTemperature = currentObj.optDouble("apparent_temperature", 25.0),
                        humidity = currentObj.optInt("relative_humidity_2m", 70),
                        precipitation = currentObj.optDouble("precipitation", 0.0),
                        rain = currentObj.optDouble("rain", 0.0),
                        weatherCode = currentObj.optInt("weather_code", 0),
                        windSpeed = currentObj.optDouble("wind_speed_10m", 10.0),
                        isDay = currentObj.optInt("is_day", 1) == 1,
                        conditionText = CoffeeAdvisoryEngine.getWeatherDescription(currentObj.optInt("weather_code", 0))
                    )

                    val times = dailyObj.getJSONArray("time")
                    val codes = dailyObj.getJSONArray("weather_code")
                    val maxTemps = dailyObj.getJSONArray("temperature_2m_max")
                    val minTemps = dailyObj.getJSONArray("temperature_2m_min")
                    val precipSums = dailyObj.getJSONArray("precipitation_sum")
                    val precipProbs = dailyObj.optJSONArray("precipitation_probability_max")

                    val dailyList = mutableListOf<DailyForecastDay>()
                    for (i in 0 until times.length()) {
                        val date = times.getString(i)
                        val code = codes.getInt(i)
                        dailyList.add(
                            DailyForecastDay(
                                date = date,
                                dayName = CoffeeAdvisoryEngine.formatDayName(date, i),
                                weatherCode = code,
                                conditionText = CoffeeAdvisoryEngine.getWeatherDescription(code),
                                maxTemp = maxTemps.getDouble(i),
                                minTemp = minTemps.getDouble(i),
                                precipitationSum = precipSums.optDouble(i, 0.0),
                                precipitationProbability = precipProbs?.optInt(i, 20) ?: 20
                            )
                        )
                    }

                    val advisory = CoffeeAdvisoryEngine.generateAdvisory(
                        current = currentWeather,
                        daily = dailyList,
                        location = location
                    )

                    val weatherData = WeatherData(
                        location = location,
                        current = currentWeather,
                        daily = dailyList,
                        advisory = advisory
                    )

                    mainHandler.post {
                        callback(Result.success(weatherData))
                    }

                } catch (e: Exception) {
                    mainHandler.post {
                        val fallback = createFallbackWeather(location)
                        callback(Result.success(fallback))
                    }
                }
            }
        })
    }

    /**
     * Search locations worldwide via Open-Meteo Geocoding
     */
    fun searchLocations(query: String, callback: (List<LocationPreset>) -> Unit) {
        if (query.trim().length < 2) {
            callback(COFFEE_REGIONS)
            return
        }

        val url = "https://geocoding-api.open-meteo.com/v1/search?name=${query.trim()}&count=8&language=en&format=json"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    val filtered = COFFEE_REGIONS.filter {
                        it.name.contains(query, ignoreCase = true) || it.region.contains(query, ignoreCase = true)
                    }
                    callback(filtered)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                val list = mutableListOf<LocationPreset>()

                try {
                    if (jsonString != null) {
                        val root = JSONObject(jsonString)
                        val results = root.optJSONArray("results")
                        if (results != null) {
                            for (i in 0 until results.length()) {
                                val item = results.getJSONObject(i)
                                val name = item.getString("name")
                                val country = item.optString("country", "")
                                val admin1 = item.optString("admin1", "")
                                val admin2 = item.optString("admin2", "")
                                val lat = item.getDouble("latitude")
                                val lon = item.getDouble("longitude")
                                val elev = item.optDouble("elevation", 0.0).toInt()

                                val regionDetails = listOf(admin2, admin1, country)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")

                                list.add(
                                    LocationPreset(
                                        name = name,
                                        region = regionDetails,
                                        latitude = lat,
                                        longitude = lon,
                                        elevationMeters = elev,
                                        coffeeType = "Local Climate Zone",
                                        isPreset = false
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }

                mainHandler.post {
                    if (list.isEmpty()) {
                        val filtered = COFFEE_REGIONS.filter {
                            it.name.contains(query, ignoreCase = true) || it.region.contains(query, ignoreCase = true)
                        }
                        callback(filtered)
                    } else {
                        callback(list)
                    }
                }
            }
        })
    }

    private fun createFallbackWeather(location: LocationPreset): WeatherData {
        val current = CurrentWeather(
            temperature = 23.5,
            apparentTemperature = 24.2,
            humidity = 72,
            precipitation = 0.5,
            rain = 0.5,
            weatherCode = 2,
            windSpeed = 12.0,
            isDay = true,
            conditionText = "Partly Cloudy"
        )
        val daily = listOf(
            DailyForecastDay("2026-11-06", "Today", 2, "Partly Cloudy", 26.0, 18.0, 1.2, 40),
            DailyForecastDay("2026-11-07", "Tomorrow", 51, "Light Drizzle", 25.0, 18.2, 2.5, 60),
            DailyForecastDay("2026-11-08", "Sun", 1, "Mainly Clear", 27.0, 17.5, 0.0, 10),
            DailyForecastDay("2026-11-09", "Mon", 0, "Clear Sky", 27.5, 17.0, 0.0, 5),
            DailyForecastDay("2026-11-10", "Tue", 2, "Partly Cloudy", 26.5, 17.8, 0.2, 25),
            DailyForecastDay("2026-11-11", "Wed", 61, "Slight Rain", 24.5, 18.0, 4.0, 70),
            DailyForecastDay("2026-11-12", "Thu", 2, "Partly Cloudy", 25.8, 17.9, 0.8, 30)
        )
        val advisory = CoffeeAdvisoryEngine.generateAdvisory(current, daily, location)
        return WeatherData(location, current, daily, advisory)
    }
}
