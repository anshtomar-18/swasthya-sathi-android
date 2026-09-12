package com.swasthyasathi.app.data.model

import com.google.gson.annotations.SerializedName

data class HourlyForecastPoint(
    @SerializedName("time") val time: String = "",
    @SerializedName("hourLabel") val hourLabel: String = "",
    @SerializedName("temperatureC") val temperatureC: Int = 32,
    @SerializedName("feelsLikeC") val feelsLikeC: Int = 36,
    @SerializedName("humidityPct") val humidityPct: Int = 60,
    @SerializedName("aqi") val aqi: Int = 120
)

data class EnvironmentalTelemetry(
    @SerializedName("latitude") val latitude: Double = 28.6139,
    @SerializedName("longitude") val longitude: Double = 77.2090,
    @SerializedName("locationName") val locationName: String = "New Delhi, Delhi NCR",
    @SerializedName("temperatureC") val temperatureC: Int = 34,
    @SerializedName("feelsLikeC") val feelsLikeC: Int = 39,
    @SerializedName("humidityPct") val humidityPct: Int = 62,
    @SerializedName("pressureHpa") val pressureHpa: Int = 1012,
    @SerializedName("windSpeedKmh") val windSpeedKmh: Int = 14,
    @SerializedName("weatherCode") val weatherCode: Int = 1,
    @SerializedName("weatherDescription") val weatherDescription: String = "Mainly Clear / Hazy",
    @SerializedName("aqi") val aqi: Int = 185,
    @SerializedName("aqiCategory") val aqiCategory: String = "Poor",
    @SerializedName("pm25") val pm25: Int = 68,
    @SerializedName("pm10") val pm10: Int = 112,
    @SerializedName("hourly") val hourly: List<HourlyForecastPoint> = emptyList(),
    @SerializedName("source") val source: String = "Open-Meteo Verified Telemetry",
    @SerializedName("timestamp") val timestamp: String = "",
    @SerializedName("isCached") val isCached: Boolean = false
)

data class CityPreset(
    val name: String,
    val state: String,
    val lat: Double,
    val lon: Double
)

val DEFAULT_INDIAN_CITIES = listOf(
    CityPreset("New Delhi", "Delhi NCR", 28.6139, 77.2090),
    CityPreset("Mumbai", "Maharashtra", 19.0760, 72.8777),
    CityPreset("Bengaluru", "Karnataka", 12.9716, 77.5946),
    CityPreset("Kolkata", "West Bengal", 22.5726, 88.3639),
    CityPreset("Chennai", "Tamil Nadu", 13.0827, 80.2707),
    CityPreset("Jaipur", "Rajasthan", 26.9124, 75.7873),
    CityPreset("Ahmedabad", "Gujarat", 23.0225, 72.5714),
    CityPreset("Lucknow", "Uttar Pradesh", 26.8467, 80.9462)
)
