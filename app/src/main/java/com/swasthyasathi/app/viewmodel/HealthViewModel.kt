package com.swasthyasathi.app.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swasthyasathi.app.data.engine.RiskEngine
import com.swasthyasathi.app.data.model.*
import com.swasthyasathi.app.data.network.RetrofitClient
import com.swasthyasathi.app.data.repository.ProfileRepository
import com.swasthyasathi.app.data.repository.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository(application)
    private val weatherRepository = WeatherRepository()

    // Profile State
    val userProfile: StateFlow<UserProfile> = profileRepository.profileFlow

    // Consumer Profile Drawer State
    private val _isProfileDrawerOpen = MutableStateFlow(false)
    val isProfileDrawerOpen: StateFlow<Boolean> = _isProfileDrawerOpen.asStateFlow()

    // Location Search Modal State
    private val _isLocationSearchOpen = MutableStateFlow(false)
    val isLocationSearchOpen: StateFlow<Boolean> = _isLocationSearchOpen.asStateFlow()

    private val _isLocatingGps = MutableStateFlow(false)
    val isLocatingGps: StateFlow<Boolean> = _isLocatingGps.asStateFlow()

    // Location State
    private val _currentCity = MutableStateFlow(DEFAULT_INDIAN_CITIES[0])
    val currentCity: StateFlow<CityPreset> = _currentCity.asStateFlow()

    // Hydration State
    private val _waterDrankMl = MutableStateFlow(1750)
    val waterDrankMl: StateFlow<Int> = _waterDrankMl.asStateFlow()

    private val _orsDrankMl = MutableStateFlow(500)
    val orsDrankMl: StateFlow<Int> = _orsDrankMl.asStateFlow()

    private val _targetWaterMl = MutableStateFlow(3000)
    val targetWaterMl: StateFlow<Int> = _targetWaterMl.asStateFlow()

    private val _targetOrsMl = MutableStateFlow(800)
    val targetOrsMl: StateFlow<Int> = _targetOrsMl.asStateFlow()

    // Cooling Shelters State
    private val _shelters = MutableStateFlow(DEFAULT_COOLING_SHELTERS)
    val shelters: StateFlow<List<CoolingShelter>> = _shelters.asStateFlow()

    private val _selectedShelterOnMap = MutableStateFlow<CoolingShelter?>(DEFAULT_COOLING_SHELTERS[0])
    val selectedShelterOnMap: StateFlow<CoolingShelter?> = _selectedShelterOnMap.asStateFlow()

    // Telemetry State
    private val _telemetry = MutableStateFlow(EnvironmentalTelemetry())
    val telemetry: StateFlow<EnvironmentalTelemetry> = _telemetry.asStateFlow()

    // Risk Engine Result State
    private val _riskResult = MutableStateFlow(RiskEngineResult())
    val riskResult: StateFlow<RiskEngineResult> = _riskResult.asStateFlow()

    // Disaster Warnings State
    private val _disasterWarnings = MutableStateFlow<List<DisasterWarning>>(emptyList())
    val disasterWarnings: StateFlow<List<DisasterWarning>> = _disasterWarnings.asStateFlow()

    // Offline / Demo Mode State
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Interactive 60fps Simulator State
    private val _isSimulatorActive = MutableStateFlow(false)
    val isSimulatorActive: StateFlow<Boolean> = _isSimulatorActive.asStateFlow()

    private val _simulatedTemp = MutableStateFlow(38f)
    val simulatedTemp: StateFlow<Float> = _simulatedTemp.asStateFlow()

    private val _simulatedAqi = MutableStateFlow(220f)
    val simulatedAqi: StateFlow<Float> = _simulatedAqi.asStateFlow()

    // Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // SOS State
    private val _isSosOpen = MutableStateFlow(false)
    val isSosOpen: StateFlow<Boolean> = _isSosOpen.asStateFlow()

    private val _sosStatus = MutableStateFlow("idle") // idle, broadcasting, sent
    val sosStatus: StateFlow<String> = _sosStatus.asStateFlow()

    // AI Chat State
    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.USER,
                text = "I've been feeling a dull tension headache and dry throat since stepping outdoors.",
                timestamp = "1:15 PM"
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "Assessing local microclimate: Barometric pressure drop combined with high ambient thermal index and PM2.5 causes cranial vasodilation and mucosal dryness. Immediate action: Drink 400 mL electrolyte fluid and rest in a shaded or indoor filtered room.",
                timestamp = "1:16 PM"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        // Collect profile changes and re-evaluate risk
        viewModelScope.launch {
            userProfile.collect { profile ->
                recomputeRisk(profile, _telemetry.value)
            }
        }
        refreshData()
    }

    fun selectCity(city: CityPreset) {
        _currentCity.value = city
        updateSheltersForLocation(city.name, city.lat, city.lon)
        refreshData()
    }

    fun selectCustomLocation(rawQuery: String) {
        val customCity = createCustomCity(rawQuery)
        selectCity(customCity)
    }

    fun setLocationSearchOpen(open: Boolean) {
        _isLocationSearchOpen.value = open
    }

    fun selectShelterOnMap(shelter: CoolingShelter?) {
        _selectedShelterOnMap.value = shelter
    }

    fun detectRealLocation(context: Context, onPermissionDenied: () -> Unit = {}) {
        viewModelScope.launch {
            _isLocatingGps.value = true
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (!hasFine && !hasCoarse) {
                _isLocatingGps.value = false
                onPermissionDenied()
                val fallbackCity = CityPreset("Connaught Place (GPS Sensor)", "New Delhi", 28.6328, 77.2197, "Simulated GPS Beacon")
                selectCity(fallbackCity)
                return@launch
            }

            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                var bestLoc: Location? = null
                if (lm != null) {
                    val gpsLoc = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
                    val netLoc = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }
                    val passiveLoc = try { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (e: SecurityException) { null }

                    bestLoc = listOfNotNull(gpsLoc, netLoc, passiveLoc).maxByOrNull { it.time }
                }

                if (bestLoc != null) {
                    val lat = bestLoc.latitude
                    val lon = bestLoc.longitude
                    var resolvedName = "GPS Fix (${String.format(Locale.US, "%.3f", lat)}°N, ${String.format(Locale.US, "%.3f", lon)}°E)"
                    var resolvedState = "Live GPS Coordinates"

                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val locality = addr.locality ?: addr.subAdminArea ?: addr.subLocality
                            val admin = addr.adminArea ?: ""
                            if (!locality.isNullOrBlank()) {
                                resolvedName = "$locality (GPS)"
                                resolvedState = if (admin.isNotBlank()) admin else "Current Location"
                            }
                        }
                    } catch (e: Exception) {
                        // Geocoder offline fallback
                    }

                    val gpsCity = CityPreset(resolvedName, resolvedState, lat, lon, "Active GPS Telemetry")
                    selectCity(gpsCity)
                } else {
                    val fallbackCity = CityPreset("Connaught Place (GPS Locked)", "New Delhi", 28.6328, 77.2197, "Simulated GPS Beacon")
                    selectCity(fallbackCity)
                }
            } catch (e: Exception) {
                val fallbackCity = CityPreset("Connaught Place (GPS Fallback)", "New Delhi", 28.6328, 77.2197, "Simulated GPS Beacon")
                selectCity(fallbackCity)
            } finally {
                _isLocatingGps.value = false
            }
        }
    }

    private fun updateSheltersForLocation(cityName: String, lat: Double, lon: Double) {
        val cleanName = cityName.replace(Regex("\\(.*\\)"), "").trim()
        _shelters.value = listOf(
            CoolingShelter(
                id = "hub-01",
                name = "$cleanName Climate Relief Hub",
                category = "cooling",
                distanceMeters = 320,
                address = "$cleanName Central Sector Arcade",
                tempC = 25f,
                status = "OPEN",
                amenities = listOf("8 Free Recliners", "Cold Misting Fans", "Free ORS Packets", "Phone Charge Point"),
                verifiedTime = "2 mins ago",
                relX = 0.72f,
                relY = 0.28f,
                canopyPct = 84,
                shadeReductionC = 5.5f,
                lat = lat + 0.002,
                lon = lon + 0.002
            ),
            CoolingShelter(
                id = "piao-01",
                name = "$cleanName Jal Sewa Dispenser",
                category = "water",
                distanceMeters = 180,
                address = "$cleanName Outer Transit Corridor",
                tempC = 14f,
                status = "ACTIVE",
                amenities = listOf("100% RO Purified (TDS 68)", "4 Food-Grade Steel Taps", "Free Bottle Refills", "Wheelchair Ramp"),
                verifiedTime = "Tested Pure 13:40",
                relX = 0.35f,
                relY = 0.38f,
                canopyPct = 88,
                shadeReductionC = 6.0f,
                lat = lat - 0.0015,
                lon = lon - 0.002
            ),
            CoolingShelter(
                id = "clinic-01",
                name = "$cleanName Mohalla Health Clinic",
                category = "clinic",
                distanceMeters = 550,
                address = "$cleanName Metro Gate 2 Lane",
                tempC = 23f,
                status = "DR ON SITE",
                amenities = listOf("Central Air Conditioned Ward", "Heatstroke Saline IV Supplies", "Instant Vitals Check (BP/SpO2)", "Free Paracetamol"),
                verifiedTime = "Live On Site",
                doctorOnSite = "Dr. Kavita Sharma, MD",
                relX = 0.65f,
                relY = 0.76f,
                canopyPct = 78,
                shadeReductionC = 4.8f,
                lat = lat - 0.003,
                lon = lon + 0.001
            ),
            CoolingShelter(
                id = "hub-02",
                name = "$cleanName Shaded Transit Lounge",
                category = "cooling",
                distanceMeters = 680,
                address = "$cleanName Underground Concourse",
                tempC = 24f,
                status = "OPEN",
                amenities = listOf("Continuous Shaded Canopy", "Industrial Misting Fans", "Chilled Electrolyte Station"),
                verifiedTime = "10 mins ago",
                relX = 0.22f,
                relY = 0.68f,
                canopyPct = 90,
                shadeReductionC = 6.5f,
                lat = lat - 0.002,
                lon = lon - 0.003
            )
        )
        _selectedShelterOnMap.value = _shelters.value.firstOrNull()
    }

    fun setProfileDrawerOpen(open: Boolean) {
        _isProfileDrawerOpen.value = open
    }

    fun logWater(amountMl: Int) {
        _waterDrankMl.value = _waterDrankMl.value + amountMl
    }

    fun logOrs(amountMl: Int) {
        _orsDrankMl.value = _orsDrankMl.value + amountMl
    }

    fun undoHydration(waterAmount: Int = 250, orsAmount: Int = 0) {
        _waterDrankMl.value = maxOf(0, _waterDrankMl.value - waterAmount)
        _orsDrankMl.value = maxOf(0, _orsDrankMl.value - orsAmount)
    }

    fun resetHydration() {
        _waterDrankMl.value = 0
        _orsDrankMl.value = 0
    }

    fun detectGps() {
        // Direct simulation shortcut
        selectCity(CityPreset("Connaught Place (GPS Locked)", "New Delhi", 28.6328, 77.2197, "Active GPS Beacon"))
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        refreshData()
    }

    fun toggleSimulatorMode() {
        _isSimulatorActive.value = !_isSimulatorActive.value
        applyCurrentTelemetryState()
    }

    fun updateSimulatorValues(temp: Float, aqi: Float) {
        _simulatedTemp.value = temp
        _simulatedAqi.value = aqi
        if (_isSimulatorActive.value) {
            applyCurrentTelemetryState()
        }
    }

    fun setSimulatorPreset(temp: Float, aqi: Float) {
        _isSimulatorActive.value = true
        _simulatedTemp.value = temp
        _simulatedAqi.value = aqi
        applyCurrentTelemetryState()
    }

    private fun applyCurrentTelemetryState() {
        if (_isSimulatorActive.value) {
            val base = _telemetry.value
            val simulatedData = base.copy(
                temperatureC = _simulatedTemp.value.toInt(),
                feelsLikeC = (_simulatedTemp.value + 4f).toInt(),
                aqi = _simulatedAqi.value.toInt(),
                pm25 = (_simulatedAqi.value * 0.45f).toInt()
            )
            _telemetry.value = simulatedData
            recomputeRisk(userProfile.value, simulatedData)
        } else {
            refreshData()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            val city = _currentCity.value
            val isOffline = _isOfflineMode.value

            val result = weatherRepository.getTelemetry(
                lat = city.lat,
                lon = city.lon,
                locationName = "${city.name}, ${city.state}",
                forceOffline = isOffline
            )

            result.onSuccess { data ->
                _telemetry.value = data
                recomputeRisk(userProfile.value, data)
            }
            _isLoading.value = false
        }
    }

    private fun recomputeRisk(profile: UserProfile, data: EnvironmentalTelemetry) {
        // Run deterministic calculation on-device
        val computed = RiskEngine.computeRisk(
            temperatureC = data.temperatureC.toDouble(),
            feelsLikeC = data.feelsLikeC.toDouble(),
            humidityPct = data.humidityPct.toDouble(),
            aqi = data.aqi.toDouble(),
            profile = profile
        )
        _riskResult.value = computed

        // Run disaster warnings calculation
        val warnings = RiskEngine.computeDisasterWarnings(
            temperatureC = data.temperatureC.toDouble(),
            feelsLikeC = data.feelsLikeC.toDouble(),
            humidityPct = data.humidityPct.toDouble(),
            aqi = data.aqi.toDouble(),
            weatherCode = data.weatherCode,
            weatherDescription = data.weatherDescription,
            pm25 = data.pm25.toDouble()
        )
        _disasterWarnings.value = warnings
    }

    fun saveProfile(profile: UserProfile) {
        profileRepository.saveProfile(profile)
    }

    fun applyPreset(preset: DemoPreset) {
        profileRepository.saveProfile(preset.profile)
        // Also switch to city matching preset
        val matchedCity = DEFAULT_INDIAN_CITIES.find {
            preset.profile.location.contains(it.name, ignoreCase = true)
        }
        if (matchedCity != null) {
            _currentCity.value = matchedCity
            refreshData()
        }
    }

    fun resetProfile() {
        profileRepository.resetProfile()
    }

    fun setChatOpen(open: Boolean) {
        _isChatOpen.value = open
    }

    fun setSosOpen(open: Boolean) {
        _isSosOpen.value = open
        if (!open) {
            _sosStatus.value = "idle"
        }
    }

    fun triggerSos() {
        viewModelScope.launch {
            _sosStatus.value = "broadcasting"
            delay(1500)
            _sosStatus.value = "sent"
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text, timestamp = now)
        _chatMessages.value = _chatMessages.value + userMsg

        // Temporary thinking placeholder
        val thinkingMsg = ChatMessage(sender = MessageSender.AI, text = "Analyzing symptoms against microclimate...", timestamp = now)
        _chatMessages.value = _chatMessages.value + thinkingMsg

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.askQuestion(AskRequest(question = text))
                val aiReply = if (response.isSuccessful && response.body() != null) {
                    response.body()!!.answer
                } else {
                    getOfflineAiReply(text)
                }
                replaceThinkingMsg(aiReply)
            } catch (e: Exception) {
                replaceThinkingMsg(getOfflineAiReply(text))
            }
        }
    }

    private fun replaceThinkingMsg(newText: String) {
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val updated = _chatMessages.value.toMutableList()
        if (updated.isNotEmpty()) {
            updated[updated.lastIndex] = ChatMessage(sender = MessageSender.AI, text = newText, timestamp = now)
            _chatMessages.value = updated
        }
    }

    private fun getOfflineAiReply(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("headache") ->
                "**Clinical Advisory**: High ambient temperature and elevated PM2.5 cause cranial vasodilation and mucosal dryness. Ingest 400 mL electrolyte fluid and rest indoors in filtered air."
            lower.contains("breath") || lower.contains("asthma") ->
                "**Clinical Advisory**: Elevated AQI triggers acute bronchial reactivity. Relocate to indoor air conditioning, utilize your prescribed inhaler, and wear an N95 respirator if stepping out."
            lower.contains("heat") || lower.contains("sweat") ->
                "**Clinical Advisory**: High humidity halts sweat evaporation. Dosing target: 350 mL electrolyte fluid every 30 mins. Take shaded cooling pauses."
            else ->
                "**Clinical Assessment**: Atmospheric heat stress and particulate air pollutants place systemic strain on cardio-respiratory balance. Maintain hydration (250 mL/hr) and monitor local AQI."
        }
    }
}
