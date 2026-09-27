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
import com.swasthyasathi.app.ai.AIRepository
import com.swasthyasathi.app.ai.AppLanguage
import com.swasthyasathi.app.ai.LanguageManager
import com.swasthyasathi.app.ai.VoiceAssistant
import com.swasthyasathi.app.ai.VoiceState
import com.swasthyasathi.app.data.engine.RiskEngine
import com.swasthyasathi.app.data.model.*
import com.swasthyasathi.app.data.repository.ProfileRepository
import com.swasthyasathi.app.data.repository.WeatherRepository
import com.swasthyasathi.app.notifications.HealthAlertManager
import com.swasthyasathi.app.sos.SOSEvent
import com.swasthyasathi.app.sos.SOSManager
import com.swasthyasathi.app.sos.SOSState
import com.swasthyasathi.app.wearable.WatchPacket
import com.swasthyasathi.app.wearable.WearableConnectionStatus
import com.swasthyasathi.app.wearable.WearableManager
import com.swasthyasathi.app.wearable.WearableTelemetry
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
    private val aiRepository = AIRepository()
    private val wearableManager = WearableManager(application)
    private val sosManager = SOSManager(application)
    private val alertManager = HealthAlertManager(application)
    private val voiceAssistant = VoiceAssistant(application)

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

    // Environmental Telemetry State
    private val _telemetry = MutableStateFlow(EnvironmentalTelemetry())
    val telemetry: StateFlow<EnvironmentalTelemetry> = _telemetry.asStateFlow()

    // Wearable Telemetry State
    val wearableTelemetry: StateFlow<WearableTelemetry> = wearableManager.telemetryState
    val watchConnectionStatus: StateFlow<WearableConnectionStatus> = wearableManager.connectionStatus
    val watchErrorMessage: StateFlow<String?> = wearableManager.lastErrorMessage
    val isDemoWearableMode: StateFlow<Boolean> = wearableManager.isDemoMode

    // Risk Engine Result State
    private val _riskResult = MutableStateFlow(RiskEngineResult())
    val riskResult: StateFlow<RiskEngineResult> = _riskResult.asStateFlow()

    // Disaster Warnings State
    private val _disasterWarnings = MutableStateFlow<List<DisasterWarning>>(emptyList())
    val disasterWarnings: StateFlow<List<DisasterWarning>> = _disasterWarnings.asStateFlow()

    // Offline / Connectivity State
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Demo Wearable Control Dialog State
    private val _isDemoWearableOpen = MutableStateFlow(false)
    val isDemoWearableOpen: StateFlow<Boolean> = _isDemoWearableOpen.asStateFlow()

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

    val sosState: StateFlow<SOSState> = sosManager.sosState
    val pendingSosCount: StateFlow<Int> = sosManager.queue.pendingSosCount
    val isDemoSosMode: StateFlow<Boolean> = sosManager.isDemoMode
    val lastQueuedEvent: StateFlow<SOSEvent?> = sosManager.lastQueuedEvent

    // AI Chat & Multilingual State
    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

    val selectedLanguage: StateFlow<AppLanguage> = LanguageManager.currentLanguage
    private val _isVoiceRepliesEnabled = MutableStateFlow(false)
    val isVoiceRepliesEnabled: StateFlow<Boolean> = _isVoiceRepliesEnabled.asStateFlow()

    val voiceState: StateFlow<VoiceState> = voiceAssistant.voiceState
    val recognizedText: StateFlow<String> = voiceAssistant.recognizedText

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.USER,
                text = "I've been feeling dizziness and a dry throat since stepping outdoors into peak solar heat.",
                timestamp = "1:15 PM"
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "Clinical Assessment: Ambient thermal index (39°C feels-like) combined with elevated heart rate (118 BPM) indicates acute heat strain. Immediate action: Ingest 400 mL electrolyte fluid and rest in shade.",
                timestamp = "1:16 PM",
                sources = listOf(
                    mapOf(
                        "title" to "IMD Thermal Index & Occupational Heat Guidelines",
                        "year" to "2024",
                        "source" to "SwasthyaSathi RAG Knowledge Base"
                    )
                )
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        // Collect profile changes and re-evaluate risk
        viewModelScope.launch {
            userProfile.collect { profile ->
                recomputeRisk(profile, _telemetry.value, wearableTelemetry.value)
            }
        }
        // Collect wearable changes and re-evaluate risk + trigger proactive alerts
        viewModelScope.launch {
            wearableTelemetry.collect { wearable ->
                recomputeRisk(userProfile.value, _telemetry.value, wearable)
                checkProactiveWearableAlerts(wearable)
            }
        }
        // Collect real-time SOS notifications emitted by the ESP32-S3 Watch
        viewModelScope.launch {
            wearableManager.sosEvents.collect { sosPacket ->
                handleWatchSosPacket(sosPacket)
            }
        }
        refreshData()
    }

    private fun checkProactiveWearableAlerts(wearable: WearableTelemetry) {
        if (wearable.fallDetected) {
            alertManager.sendProactiveHealthAlert(
                title = "🚨 FALL DETECTED ALERT!",
                message = "Your wearable watch registered an abrupt fall impact. Emergency SOS signal queued.",
                isEmergency = true
            )
            triggerEmergencySOS("Automated Fall Detection Alarm")
        } else if (wearable.heartRate > 120 && _telemetry.value.temperatureC >= 36) {
            alertManager.sendProactiveHealthAlert(
                title = "⚠ High Heat & Heart Rate Warning",
                message = "Heart rate reached ${wearable.heartRate} BPM during ${telemetry.value.temperatureC}°C extreme heat. Seek cool shade immediately.",
                isEmergency = false
            )
        }
    }

    private fun handleWatchSosPacket(packet: WatchPacket) {
        val prof = userProfile.value
        val w = wearableTelemetry.value
        val r = riskResult.value

        val hasValidGps = packet.gps == true && packet.latitude != null && packet.longitude != null && packet.latitude != 0.0
        val locationDesc = if (hasValidGps) {
            "Watch GPS (${String.format(Locale.US, "%.4f", packet.latitude)}°N, ${String.format(Locale.US, "%.4f", packet.longitude)}°E • Satellites: ${packet.satellites ?: 0})"
        } else {
            "Location unavailable (Watch GPS Searching)"
        }

        val event = SOSEvent(
            userId = prof.abhaId,
            locationName = locationDesc,
            latitude = if (hasValidGps) packet.latitude!! else 0.0,
            longitude = if (hasValidGps) packet.longitude!! else 0.0,
            heartRate = packet.hr ?: w.heartRate,
            spo2 = packet.spo2 ?: w.spo2,
            bodyTemperature = (packet.temperature ?: w.bodyTemperature.toDouble()).toFloat(),
            ambientTemperature = (packet.temperature ?: w.ambientTemperature.toDouble()).toFloat(),
            riskLevel = r.level,
            detectedCondition = "SOS received from watch [HR: ${packet.hr ?: w.heartRate} bpm, SpO2: ${packet.spo2 ?: w.spo2}%, Temp: ${packet.temperature ?: "--"}°C] at ${packet.timestamp ?: "Just now"}",
            emergencyContactName = prof.emergencyContactName,
            emergencyContactPhone = prof.emergencyContactPhone
        )

        alertManager.sendProactiveHealthAlert(
            title = "🚨 EMERGENCY SOS FROM WATCH!",
            message = "Physical SOS button was triggered on your SwasthyaSathi Watch. Emergency distress payload active.",
            isEmergency = true
        )

        viewModelScope.launch {
            sosManager.triggerEmergencySOS(event, _isOfflineMode.value)
            _isSosOpen.value = true
        }
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

    fun resetProfile() {
        profileRepository.resetProfile()
    }

    fun setDemoWearableOpen(open: Boolean) {
        _isDemoWearableOpen.value = open
    }

    fun connectWatch() {
        wearableManager.connectWatch()
    }

    fun disconnectWatch() {
        wearableManager.disconnectWatch()
    }

    fun stopWatchScan() {
        wearableManager.stopScan()
    }

    fun sendWatchCommand(command: String): Boolean {
        return wearableManager.sendCommand(command)
    }

    fun setDemoWearableMode(active: Boolean) {
        wearableManager.setDemoMode(active)
    }

    fun isBluetoothEnabled(): Boolean = wearableManager.isBluetoothEnabled()

    fun hasRequiredBlePermissions(): Boolean = wearableManager.hasRequiredPermissions()

    fun updateWearableTelemetry(builder: (WearableTelemetry) -> WearableTelemetry) {
        wearableManager.updateTelemetry(builder)
    }

    fun setLanguage(language: AppLanguage) {
        LanguageManager.setLanguage(language)
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
            recomputeRisk(userProfile.value, simulatedData, wearableTelemetry.value)
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
                recomputeRisk(userProfile.value, data, wearableTelemetry.value)
                sosManager.onConnectivityRestored()
            }
            _isLoading.value = false
        }
    }

    private fun recomputeRisk(profile: UserProfile, envData: EnvironmentalTelemetry, wearableData: WearableTelemetry) {
        val effectiveTemp = if (wearableData.isRealWatchData && wearableData.ambientTemperature > 0f) {
            wearableData.ambientTemperature.toDouble()
        } else {
            envData.temperatureC.toDouble()
        }
        val effectiveHumidity = if (wearableData.isRealWatchData && wearableData.humidity > 0) {
            wearableData.humidity.toDouble()
        } else {
            envData.humidityPct.toDouble()
        }

        val computed = RiskEngine.computeRisk(
            temperatureC = effectiveTemp,
            feelsLikeC = envData.feelsLikeC.toDouble(),
            humidityPct = effectiveHumidity,
            aqi = envData.aqi.toDouble(),
            profile = profile
        )
        _riskResult.value = computed

        val warnings = RiskEngine.computeDisasterWarnings(
            temperatureC = effectiveTemp,
            feelsLikeC = envData.feelsLikeC.toDouble(),
            humidityPct = effectiveHumidity,
            aqi = envData.aqi.toDouble(),
            weatherCode = envData.weatherCode,
            weatherDescription = envData.weatherDescription,
            pm25 = envData.pm25.toDouble()
        )
        _disasterWarnings.value = warnings
    }

    fun saveProfile(profile: UserProfile) {
        profileRepository.saveProfile(profile)
    }

    fun applyPreset(preset: DemoPreset) {
        profileRepository.saveProfile(preset.profile)
        val matchedCity = DEFAULT_INDIAN_CITIES.find {
            preset.profile.location.contains(it.name, ignoreCase = true)
        }
        if (matchedCity != null) {
            _currentCity.value = matchedCity
            refreshData()
        }
    }

    fun setChatOpen(open: Boolean) {
        _isChatOpen.value = open
    }

    fun setSosOpen(open: Boolean) {
        _isSosOpen.value = open
        if (!open) {
            sosManager.resetState()
        }
    }

    fun triggerEmergencySOS(customCondition: String? = null) {
        viewModelScope.launch {
            val city = _currentCity.value
            val prof = userProfile.value
            val w = wearableTelemetry.value
            val r = riskResult.value

            val event = SOSEvent(
                userId = prof.abhaId,
                locationName = "${city.name}, ${city.state}",
                latitude = city.lat,
                longitude = city.lon,
                heartRate = w.heartRate,
                spo2 = w.spo2,
                bodyTemperature = w.bodyTemperature,
                ambientTemperature = w.ambientTemperature,
                riskLevel = r.level,
                detectedCondition = customCondition ?: w.anomalySummary,
                emergencyContactName = prof.emergencyContactName,
                emergencyContactPhone = prof.emergencyContactPhone
            )

            sosManager.triggerEmergencySOS(event, _isOfflineMode.value)
        }
    }

    fun resetSosState() {
        sosManager.resetState()
    }

    fun startVoiceRecognition() {
        voiceAssistant.startListening(selectedLanguage.value)
    }

    fun createSpeechIntent(): android.content.Intent {
        return voiceAssistant.createRecognizerIntent(selectedLanguage.value)
    }

    fun onSpeechActivityResult(text: String) {
        voiceAssistant.onSpeechActivityResult(text)
    }

    fun stopVoiceRecognition() {
        voiceAssistant.stopListening()
    }

    fun clearRecognizedText() {
        voiceAssistant.clearRecognizedText()
    }

    fun speakResponse(text: String) {
        if (voiceState.value == VoiceState.SPEAKING) {
            voiceAssistant.stopSpeaking()
        } else {
            voiceAssistant.speak(text, selectedLanguage.value, force = true)
        }
    }

    fun stopSpeaking() {
        voiceAssistant.stopSpeaking()
    }

    fun toggleVoiceReplies() {
        val nextState = !_isVoiceRepliesEnabled.value
        _isVoiceRepliesEnabled.value = nextState
        voiceAssistant.setVoiceRepliesEnabled(nextState)
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val detectedLang = LanguageManager.detectLanguageFromText(text)
        if (detectedLang != selectedLanguage.value) {
            LanguageManager.setLanguage(detectedLang)
        }

        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text, timestamp = now)
        _chatMessages.value = _chatMessages.value + userMsg

        val thinkingText = when (detectedLang) {
            AppLanguage.HINDI -> "माइक्रोक्लाइमेट और स्वास्थ्य डेटा का विश्लेषण हो रहा है..."
            AppLanguage.BENGALI -> "মাইক্রোক্লাইমেট এবং স্বাস্থ্য তথ্য বিশ্লেষণ করা হচ্ছে..."
            else -> "Analyzing microclimate and personalized health telemetry..."
        }
        val thinkingMsg = ChatMessage(sender = MessageSender.AI, text = thinkingText, timestamp = now)
        _chatMessages.value = _chatMessages.value + thinkingMsg

        viewModelScope.launch {
            val response = aiRepository.askQuestion(
                question = text,
                language = detectedLang,
                profile = userProfile.value,
                envTelemetry = _telemetry.value,
                wearableTelemetry = wearableTelemetry.value,
                riskResult = _riskResult.value,
                isOfflineMode = _isOfflineMode.value
            )

            replaceThinkingMsg(response.answer, response.sources)
            if (_isVoiceRepliesEnabled.value) {
                voiceAssistant.speak(response.answer, detectedLang)
            }
        }
    }

    private fun replaceThinkingMsg(newText: String, sources: List<Map<String, Any>> = emptyList()) {
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val updated = _chatMessages.value.toMutableList()
        if (updated.isNotEmpty()) {
            updated[updated.lastIndex] = ChatMessage(sender = MessageSender.AI, text = newText, timestamp = now, sources = sources)
            _chatMessages.value = updated
        }
    }
}
