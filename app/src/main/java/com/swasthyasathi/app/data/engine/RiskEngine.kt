package com.swasthyasathi.app.data.engine

import com.swasthyasathi.app.data.model.DisasterWarning
import com.swasthyasathi.app.data.model.RiskEngineResult
import com.swasthyasathi.app.data.model.RiskLevel
import com.swasthyasathi.app.data.model.UserProfile
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * On-Device Deterministic Personal Risk Engine (SIH26181)
 *
 * Implements pure, auditable clinical calculations directly in Kotlin.
 * Zero remote dependency, zero medical data leakage.
 */
object RiskEngine {

    fun computeRisk(
        temperatureC: Double,
        feelsLikeC: Double,
        humidityPct: Double,
        aqi: Double,
        profile: UserProfile
    ): RiskEngineResult {
        val hasSensitivity = { type: String ->
            profile.sensitivities.contains(type) || profile.diseases.any { it.contains(type, ignoreCase = true) }
        }

        // 1. Thermal Stress Sub-Score (1.0 to 4.0) - IMD / NOAA Heat Index
        val effectiveTemp = max(temperatureC, feelsLikeC)
        val thermalStress = when {
            effectiveTemp >= 43 || (temperatureC >= 38 && humidityPct >= 65) -> 4.0
            effectiveTemp >= 38 || (temperatureC >= 34 && humidityPct >= 60) -> 3.0
            effectiveTemp >= 32 || (temperatureC >= 30 && humidityPct >= 70) -> 2.0
            else -> 1.0
        }

        // 2. Particulate Strain Sub-Score (1.0 to 4.0) - CPCB NAQI Breakpoints
        val particulateStrain = when {
            aqi > 300 -> 4.0
            aqi >= 201 -> 3.0
            aqi >= 101 -> 2.0
            else -> 1.0
        }

        // Compound environmental penalty if both severe heat and high AQI co-occur
        var baseScore = max(thermalStress, particulateStrain)
        if (thermalStress >= 2.5 && particulateStrain >= 2.5) {
            baseScore += 0.5
        }

        // 3. Exposure Modifier (0.8 to 1.35) factoring outdoor hours
        val baseExposure = when (profile.outdoorActivityLevel.lowercase()) {
            "high" -> 1.2
            "moderate" -> 1.0
            else -> 0.8
        }
        val hoursBonus = when {
            profile.outdoorHours >= 8.0f -> 0.15
            profile.outdoorHours >= 5.0f -> 0.08
            profile.outdoorHours <= 2.0f -> -0.05
            else -> 0.0
        }
        val exposureMultiplier = (baseExposure + hoursBonus).coerceIn(0.75, 1.35)

        // 4. Biological Vulnerability Escalation (0.0 to 2.5)
        var vulnerabilityEscalation = 0.0

        // Age factor (from bracket or exact age)
        if (profile.exactAge >= 60 || profile.ageGroup == "60+") {
            if (thermalStress >= 2.0 || particulateStrain >= 2.0) vulnerabilityEscalation += 0.75
        } else if (profile.exactAge < 16 || profile.ageGroup == "under18") {
            if (thermalStress >= 2.5) vulnerabilityEscalation += 0.4
        } else if (profile.exactAge in 45..59 || profile.ageGroup == "41-60") {
            if (thermalStress >= 3.0 || particulateStrain >= 3.0) vulnerabilityEscalation += 0.3
        }

        // Clinical conditions and sensitivities
        if ((hasSensitivity("heat") || profile.diseases.any { it.contains("Heat", ignoreCase = true) }) && thermalStress >= 2.0) {
            vulnerabilityEscalation += 0.75
        }

        if (hasSensitivity("cardiovascular") || profile.diseases.any { it.contains("Cardiovascular", ignoreCase = true) || it.contains("Hypertension", ignoreCase = true) }) {
            if (thermalStress >= 2.5 || particulateStrain >= 2.0) vulnerabilityEscalation += 0.85
        }

        if (hasSensitivity("respiratory") || profile.diseases.any { it.contains("Asthma", ignoreCase = true) || it.contains("COPD", ignoreCase = true) || it.contains("Allergies", ignoreCase = true) }) {
            if (particulateStrain >= 2.0) vulnerabilityEscalation += 0.85
        }

        if (profile.diseases.any { it.contains("Diabetes", ignoreCase = true) || it.contains("Kidney", ignoreCase = true) }) {
            if (thermalStress >= 2.0) vulnerabilityEscalation += 0.5
        }

        // 5. Final Score & Tier Determination
        val finalScore = (baseScore * exposureMultiplier) + vulnerabilityEscalation

        val levelStr = when {
            finalScore >= 3.8 -> "Critical"
            finalScore >= 2.8 -> "High"
            finalScore >= 1.8 -> "Moderate"
            else -> "Low"
        }

        // 6. Explainable Primary Driver
        val isHeatDominant = thermalStress >= particulateStrain
        val effRounded = effectiveTemp.roundToInt()
        val aqiRounded = aqi.roundToInt()

        val primaryDriver = when (levelStr) {
            "Critical" -> {
                if (isHeatDominant) {
                    "Dangerous thermal index (${effRounded}°C feels-like) combined with your ${profile.outdoorActivityLevel} outdoor exertion and age/sensitivity parameters pushes heat exhaustion and acute dehydration risk to critical thresholds."
                } else {
                    "Toxic particulate concentration (AQI ${aqiRounded}) coupled with your sensitivity profile presents an acute hazard for airway constriction and cardiovascular overstrain."
                }
            }
            "High" -> {
                if (isHeatDominant) {
                    "High ambient heat (${effRounded}°C) with ${humidityPct.roundToInt()}% humidity accelerates sweat loss and circulatory fatigue, heightened by your outdoor exposure pattern."
                } else {
                    "Elevated particulate pollution (AQI ${aqiRounded}) exceeds safe physiological limits for your profile, increasing mucosal inflammation and respiratory reactivity."
                }
            }
            "Moderate" -> {
                if (isHeatDominant) {
                    "Warm microclimate (${effRounded}°C) may induce mild heat fatigue and dehydration during extended commutes or workouts."
                } else {
                    "Moderate particulate levels (AQI ${aqiRounded}) may cause mild throat or ocular irritation; baseline preventive hydration and pacing recommended."
                }
            }
            else -> {
                "Atmospheric conditions (${temperatureC.roundToInt()}°C, AQI ${aqiRounded}) remain within your personalized physiological tolerance baseline."
            }
        }

        // 7. Concrete Preventive Recommendations
        val recs = mutableListOf<String>()

        if (thermalStress >= 3.0 || (profile.outdoorActivityLevel == "high" && thermalStress >= 2.0)) {
            recs.add("Drink 350–400 mL of electrolyte-fortified fluid every 30 minutes. Avoid relying solely on unmineralized water during heavy sweating.")
        } else if (thermalStress >= 2.0) {
            recs.add("Maintain proactive fluid intake of 250 mL per hour, even before the sensation of thirst occurs.")
        } else {
            recs.add("Maintain baseline daily hydration target of 2.2 to 2.8 liters.")
        }

        if (levelStr == "Critical" || levelStr == "High") {
            if (thermalStress >= 2.5) {
                recs.add("Halt direct solar physical labor between 12:00 PM and 3:30 PM. Mandate 15-minute shaded cooling intervals.")
            }
        }

        if (particulateStrain >= 3.0 || (particulateStrain >= 2.0 && hasSensitivity("respiratory"))) {
            recs.add("Wear a properly fitted N95 respirator during outdoor transit. Keep domestic windows closed with indoor air filtration active.")
        } else if (particulateStrain >= 2.0) {
            recs.add("Shift vigorous cardiovascular workouts from outdoor roads to indoor spaces.")
        }

        if (hasSensitivity("cardiovascular") || profile.ageGroup == "60+") {
            if (levelStr == "High" || levelStr == "Critical") {
                recs.add("Monitor resting pulse every 2 hours. If resting heart rate exceeds 95 BPM with dizziness, retreat to cool shelter and inform your emergency contact.")
            }
        }

        if (recs.size < 2) {
            recs.add("Monitor local hourly temperature shifts before planning prolonged transit.")
        }

        return RiskEngineResult(
            level = levelStr,
            primaryDriver = primaryDriver,
            recommendations = recs.take(4),
            rawScore = finalScore,
            subScores = mapOf(
                "thermalStress" to thermalStress,
                "particulateStrain" to particulateStrain,
                "exposureMultiplier" to exposureMultiplier,
                "vulnerabilityEscalation" to vulnerabilityEscalation
            )
        )
    }

    fun computeDisasterWarnings(
        temperatureC: Double,
        feelsLikeC: Double,
        humidityPct: Double,
        aqi: Double,
        weatherCode: Int = 0,
        weatherDescription: String = "",
        pm25: Double = 0.0
    ): List<DisasterWarning> {
        val warnings = mutableListOf<DisasterWarning>()
        val effectiveTemp = max(temperatureC, feelsLikeC)

        // 1. IMD Heat Wave
        if (effectiveTemp >= 43 || temperatureC >= 42) {
            warnings.add(
                DisasterWarning(
                    id = "heat-wave-severe",
                    title = "IMD Red Alert: Severe Heat Wave",
                    severity = "critical",
                    hazardType = "heat",
                    shortExplanation = "Ambient temperature of ${temperatureC.roundToInt()}°C (Feels like ${feelsLikeC.roundToInt()}°C) exceeds critical biological thermoregulation thresholds.",
                    actionGuidance = "Suspend strenuous outdoor labor. Access shaded cooling centers and ingest electrolyte rehydration fluids."
                )
            )
        } else if (effectiveTemp >= 38 || temperatureC >= 37) {
            warnings.add(
                DisasterWarning(
                    id = "heat-wave-moderate",
                    title = "IMD Orange Alert: Heat Wave Advisory",
                    severity = "high",
                    hazardType = "heat",
                    shortExplanation = "Elevated thermal stress (${temperatureC.roundToInt()}°C, Feels like ${feelsLikeC.roundToInt()}°C) accelerates dehydration and cardiovascular fatigue.",
                    actionGuidance = "Pre-hydrate with 350-400 mL water every 40 minutes and limit direct solar exposure between 11:30 AM and 3:30 PM."
                )
            )
        }

        // 2. CPCB AQI Alert
        if (aqi >= 300) {
            warnings.add(
                DisasterWarning(
                    id = "aqi-severe",
                    title = "CPCB Red Alert: Hazardous Particulate Smog",
                    severity = "critical",
                    hazardType = "aqi",
                    shortExplanation = "Hazardous AQI of ${aqi.roundToInt()} (PM2.5: ${if (pm25 > 0) pm25.roundToInt().toString() else "Elevated"} µg/m³) triggers acute bronchial reactivity and systemic inflammation.",
                    actionGuidance = "Wear certified N95 respirators outdoors. Maintain indoor HEPA air purification and avoid open-air aerobic exercise."
                )
            )
        } else if (aqi >= 200) {
            warnings.add(
                DisasterWarning(
                    id = "aqi-poor",
                    title = "CPCB Orange Alert: Very Poor Air Quality",
                    severity = "high",
                    hazardType = "aqi",
                    shortExplanation = "High particulate smog (${aqi.roundToInt()} AQI) causes nasal and ocular mucosa irritation and exacerbates latent respiratory conditions.",
                    actionGuidance = "Sensitive groups should remain indoors with windows closed. Limit continuous outdoor exposure to under 30 minutes."
                )
            )
        }

        // 3. Storm / Monsoon Flood
        if (weatherCode >= 95) {
            warnings.add(
                DisasterWarning(
                    id = "storm-thunder",
                    title = "Convective Severe Thunderstorm & Lightning Alert",
                    severity = "high",
                    hazardType = "flood",
                    shortExplanation = "Severe convective storm activity with squally gusts and lightning discharge. High risk of localized power disruption and flash flooding.",
                    actionGuidance = "Stay clear of tall trees, power lines, and open metal structures. Remain in sturdy indoor shelter until squalls pass."
                )
            )
        } else if (weatherCode >= 80 || (weatherCode in 61..65)) {
            warnings.add(
                DisasterWarning(
                    id = "monsoon-rain",
                    title = "Intense Monsoon Precipitation & Urban Inundation Advisory",
                    severity = "moderate",
                    hazardType = "flood",
                    shortExplanation = "Heavy rainfall (${weatherDescription.ifEmpty { "Monsoon showers" }}) may overwhelm urban storm drains, leading to road waterlogging.",
                    actionGuidance = "Avoid traversing waterlogged streets due to open manhole hazards and contaminated stormwater runoff."
                )
            )
        }

        // 4. Wet-Bulb Trap
        if (humidityPct >= 75 && temperatureC >= 32) {
            warnings.add(
                DisasterWarning(
                    id = "wet-bulb-trap",
                    title = "Wet-Bulb High-Humidity Heat Trap",
                    severity = "high",
                    hazardType = "humidity",
                    shortExplanation = "High ambient humidity (${humidityPct.roundToInt()}%) arrests cutaneous sweat evaporation, causing rapid internal heat retention.",
                    actionGuidance = "Operate fans or cross-ventilation, wear lightweight breathable cotton, and apply damp cloth compresses to pulse points."
                )
            )
        }

        // 5. Baseline Normal
        if (warnings.isEmpty()) {
            warnings.add(
                DisasterWarning(
                    id = "baseline-normal",
                    title = "No Active Meteorological Disaster Alerts",
                    severity = "normal",
                    hazardType = "normal",
                    shortExplanation = "Atmospheric parameters (${temperatureC.roundToInt()}°C, AQI ${aqi.roundToInt()}) are within standard physiological baseline limits for this region.",
                    actionGuidance = "Follow standard seasonal hydration habits and daily outdoor schedules without special meteorological restrictions."
                )
            )
        }

        return warnings
    }
}
