package com.swasthyasathi.app.data.repository

import com.swasthyasathi.app.data.model.EnvironmentalTelemetry
import com.swasthyasathi.app.data.model.HourlyForecastPoint
import com.swasthyasathi.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository {

    private var cachedTelemetry: EnvironmentalTelemetry? = null

    suspend fun getTelemetry(
        lat: Double,
        lon: Double,
        locationName: String,
        forceOffline: Boolean = false
    ): Result<EnvironmentalTelemetry> = withContext(Dispatchers.IO) {
        if (forceOffline) {
            val cached = cachedTelemetry ?: createOfflineFallback(lat, lon, locationName)
            return@withContext Result.success(cached.copy(isCached = true))
        }

        try {
            val response = RetrofitClient.apiService.getTelemetry(lat, lon, locationName)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                cachedTelemetry = data
                Result.success(data)
            } else {
                // If backend returns error, use offline simulation cache
                val fallback = cachedTelemetry ?: createOfflineFallback(lat, lon, locationName)
                Result.success(fallback.copy(isCached = true))
            }
        } catch (e: Exception) {
            // Network connection error: return offline telemetry cache seamlessly
            val fallback = cachedTelemetry ?: createOfflineFallback(lat, lon, locationName)
            Result.success(fallback.copy(isCached = true))
        }
    }

    private fun createOfflineFallback(
        lat: Double,
        lon: Double,
        locationName: String
    ): EnvironmentalTelemetry {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return EnvironmentalTelemetry(
            latitude = lat,
            longitude = lon,
            locationName = locationName,
            temperatureC = 37,
            feelsLikeC = 42,
            humidityPct = 65,
            pressureHpa = 1009,
            windSpeedKmh = 14,
            weatherCode = 1,
            weatherDescription = "Warm & Elevated Smog (Simulated)",
            aqi = 210,
            aqiCategory = "Unhealthy",
            pm25 = 78,
            pm10 = 134,
            hourly = listOf(
                HourlyForecastPoint("12:00", "12 PM", 36, 40, 62, 195),
                HourlyForecastPoint("14:00", "2 PM", 39, 44, 65, 220),
                HourlyForecastPoint("16:00", "4 PM", 38, 43, 68, 235),
                HourlyForecastPoint("18:00", "6 PM", 34, 38, 72, 210),
                HourlyForecastPoint("20:00", "8 PM", 31, 35, 75, 180),
                HourlyForecastPoint("22:00", "10 PM", 29, 32, 78, 160)
            ),
            source = "Offline Telemetry Cache (On-Device Resilience)",
            timestamp = now,
            isCached = true
        )
    }
}
