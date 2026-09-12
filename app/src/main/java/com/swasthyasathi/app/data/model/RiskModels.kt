package com.swasthyasathi.app.data.model

import com.google.gson.annotations.SerializedName

enum class RiskLevel(val label: String) {
    Low("Low"),
    Moderate("Moderate"),
    High("High"),
    Critical("Critical");

    companion object {
        fun fromString(value: String): RiskLevel {
            return entries.find { it.label.equals(value, ignoreCase = true) } ?: Moderate
        }
    }
}

data class RiskEngineResult(
    @SerializedName("level") val level: String = "Moderate",
    @SerializedName("primaryDriver") val primaryDriver: String = "",
    @SerializedName("recommendations") val recommendations: List<String> = emptyList(),
    @SerializedName("rawScore") val rawScore: Double = 2.0,
    @SerializedName("subScores") val subScores: Map<String, Double> = emptyMap()
) {
    val riskLevel: RiskLevel
        get() = RiskLevel.fromString(level)
}

data class DisasterWarning(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("severity") val severity: String, // critical, high, moderate, normal
    @SerializedName("hazardType") val hazardType: String, // heat, aqi, flood, humidity, normal
    @SerializedName("shortExplanation") val shortExplanation: String,
    @SerializedName("actionGuidance") val actionGuidance: String
)
