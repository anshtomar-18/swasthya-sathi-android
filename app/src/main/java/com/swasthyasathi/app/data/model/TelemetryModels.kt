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
    val lon: Double,
    val zoneDescription: String = "Urban Core"
)

val DEFAULT_INDIAN_CITIES = listOf(
    // Delhi NCR & North
    CityPreset("New Delhi (Connaught Place)", "Delhi NCR", 28.6328, 77.2197, "Central Delhi Core"),
    CityPreset("South Delhi (Hauz Khas)", "Delhi NCR", 28.5494, 77.2001, "Urban Canopy Belt"),
    CityPreset("Noida (Sector 62)", "Uttar Pradesh", 28.6270, 77.3683, "NCR Tech Corridor"),
    CityPreset("Gurugram (Cyber City)", "Haryana", 28.4906, 77.0898, "NCR Commercial Hub"),
    CityPreset("Faridabad", "Haryana", 28.4089, 77.3178, "Industrial Sector"),
    CityPreset("Ghaziabad", "Uttar Pradesh", 28.6692, 77.4538, "NCR East Hub"),
    CityPreset("Chandigarh", "Chandigarh UT", 30.7333, 76.7794, "Shaded Sector Grid"),
    CityPreset("Ludhiana", "Punjab", 30.9010, 75.8573, "Industrial Central"),
    CityPreset("Amritsar", "Punjab", 31.6340, 74.8723, "Heritage Core"),
    CityPreset("Dehradun", "Uttarakhand", 30.3165, 78.0322, "Doon Valley Foothills"),
    CityPreset("Shimla", "Himachal Pradesh", 31.1048, 77.1734, "Hill Station Ridge"),
    CityPreset("Srinagar", "Jammu & Kashmir", 34.0837, 74.7973, "Valley Central"),
    CityPreset("Jammu", "Jammu & Kashmir", 32.7266, 74.8570, "Tawi Plains"),

    // West & Central
    CityPreset("Mumbai (Marine Drive)", "Maharashtra", 18.9438, 72.8233, "South Coastal Promenade"),
    CityPreset("Mumbai (Bandra West)", "Maharashtra", 19.0596, 72.8295, "Western Suburbs"),
    CityPreset("Pune (Shivajinagar)", "Maharashtra", 18.5314, 73.8446, "Deccan Plateau"),
    CityPreset("Nagpur", "Maharashtra", 21.1458, 79.0882, "Central India Thermal Zone"),
    CityPreset("Nashik", "Maharashtra", 19.9975, 73.7898, "Godavari Basin"),
    CityPreset("Thane", "Maharashtra", 19.2183, 72.9781, "Lakes District"),
    CityPreset("Ahmedabad", "Gujarat", 23.0225, 72.5714, "Sabarmati Riverfront"),
    CityPreset("Surat", "Gujarat", 21.1702, 72.8311, "Diamond City Core"),
    CityPreset("Vadodara", "Gujarat", 22.3072, 73.1812, "Cultural Capital"),
    CityPreset("Rajkot", "Gujarat", 22.3039, 70.8022, "Saurashtra Hub"),
    CityPreset("Jaipur (Pink City)", "Rajasthan", 26.9124, 75.7873, "Arid Urban Zone"),
    CityPreset("Jodhpur", "Rajasthan", 26.2389, 73.0243, "Thar Gateway"),
    CityPreset("Udaipur", "Rajasthan", 24.5854, 73.7125, "Lake City Core"),
    CityPreset("Kota", "Rajasthan", 25.2138, 75.8648, "Chambal Valley"),
    CityPreset("Bhopal", "Madhya Pradesh", 23.2599, 77.4126, "Upper Lake Region"),
    CityPreset("Indore", "Madhya Pradesh", 22.7196, 75.8577, "Malwa Plateau"),
    CityPreset("Gwalior", "Madhya Pradesh", 26.2183, 78.1828, "Chambal Plains"),

    // South
    CityPreset("Bengaluru (MG Road)", "Karnataka", 12.9756, 77.6066, "Central Business District"),
    CityPreset("Bengaluru (Whitefield)", "Karnataka", 12.9698, 77.7499, "East Tech Corridor"),
    CityPreset("Bengaluru (Indiranagar)", "Karnataka", 12.9784, 77.6408, "Green Canopy Suburb"),
    CityPreset("Chennai (Marina)", "Tamil Nadu", 13.0500, 80.2824, "Coastal Bay Zone"),
    CityPreset("Chennai (Anna Nagar)", "Tamil Nadu", 13.0850, 80.2100, "North-West Grid"),
    CityPreset("Hyderabad (Hitec City)", "Telangana", 17.4474, 78.3762, "Cyberabad Valley"),
    CityPreset("Hyderabad (Charminar)", "Telangana", 17.3616, 78.4747, "Old Heritage Core"),
    CityPreset("Kochi (Marine Drive)", "Kerala", 9.9816, 76.2753, "Coastal Backwaters"),
    CityPreset("Thiruvananthapuram", "Kerala", 8.5241, 76.9366, "Southern Coastal Ridge"),
    CityPreset("Kozhikode", "Kerala", 11.2588, 75.7804, "Malabar Coast"),
    CityPreset("Coimbatore", "Tamil Nadu", 11.0168, 76.9558, "Western Ghats Foothills"),
    CityPreset("Madurai", "Tamil Nadu", 9.9252, 78.1198, "Vaigai Plain"),
    CityPreset("Visakhapatnam", "Andhra Pradesh", 17.6868, 83.2185, "Eastern Naval Coast"),
    CityPreset("Vijayawada", "Andhra Pradesh", 16.5062, 80.6480, "Krishna Delta"),
    CityPreset("Panaji", "Goa", 15.4909, 73.8278, "Mandovi Estuary"),

    // East & North-East
    CityPreset("Kolkata (Park Street)", "West Bengal", 22.5516, 88.3518, "Central Metro Core"),
    CityPreset("Kolkata (Salt Lake)", "West Bengal", 22.5867, 88.4178, "Bidhannagar Tech Sector"),
    CityPreset("Howrah", "West Bengal", 22.5958, 88.2636, "Hooghly Riverfront"),
    CityPreset("Patna", "Bihar", 25.5941, 85.1376, "Ganga Basin Core"),
    CityPreset("Gaya", "Bihar", 24.7914, 85.0002, "Magadh Plains"),
    CityPreset("Lucknow (Hazratganj)", "Uttar Pradesh", 26.8500, 80.9499, "Gomti Riverside Core"),
    CityPreset("Kanpur", "Uttar Pradesh", 26.4499, 80.3319, "Industrial Riverfront"),
    CityPreset("Varanasi", "Uttar Pradesh", 25.3176, 82.9739, "Ghats Microclimate"),
    CityPreset("Agra", "Uttar Pradesh", 27.1767, 78.0081, "Yamuna Heritage Zone"),
    CityPreset("Prayagraj", "Uttar Pradesh", 25.4358, 81.8463, "Sangam Confluence"),
    CityPreset("Ranchi", "Jharkhand", 23.3441, 85.3096, "Chota Nagpur Plateau"),
    CityPreset("Jamshedpur", "Jharkhand", 22.8046, 86.2029, "Subarnarekha Valley"),
    CityPreset("Bhubaneswar", "Odisha", 20.2961, 85.8245, "Temple City Core"),
    CityPreset("Raipur", "Chhattisgarh", 21.2514, 81.6296, "Mahanadi Plains"),
    CityPreset("Guwahati", "Assam", 26.1445, 91.7362, "Brahmaputra Valley")
)

fun searchCities(query: String): List<CityPreset> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return DEFAULT_INDIAN_CITIES
    return DEFAULT_INDIAN_CITIES.filter {
        it.name.lowercase().contains(q) ||
        it.state.lowercase().contains(q) ||
        it.zoneDescription.lowercase().contains(q)
    }
}

fun createCustomCity(rawQuery: String): CityPreset {
    val cleaned = rawQuery.trim().replaceFirstChar { it.uppercase() }
    // Deterministic pseudo-coordinates near central India / Delhi base if unspecified
    val hash = kotlin.math.abs(cleaned.hashCode())
    val latOffset = ((hash % 1000) / 1000.0) * 10.0 - 5.0
    val lonOffset = (((hash / 1000) % 1000) / 1000.0) * 10.0 - 5.0
    val lat = 24.0 + latOffset
    val lon = 78.0 + lonOffset
    return CityPreset(
        name = cleaned,
        state = "Custom Search",
        lat = lat,
        lon = lon,
        zoneDescription = "User Defined Location"
    )
}
