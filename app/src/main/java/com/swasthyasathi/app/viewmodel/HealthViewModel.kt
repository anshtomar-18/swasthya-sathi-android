package com.swasthyasathi.app.viewmodel

import android.app.Application
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

    // Location State
    private val _currentCity = MutableStateFlow(DEFAULT_INDIAN_CITIES[0])
    val currentCity: StateFlow<CityPreset> = _currentCity.asStateFlow()

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
        refreshData()
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
