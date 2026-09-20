package com.swasthyasathi.app.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val locale: Locale) {
    ENGLISH("en", "English", Locale.ENGLISH),
    HINDI("hi", "हिन्दी", Locale("hi", "IN")),
    BENGALI("bn", "বাংলা", Locale("bn", "IN"));

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object LanguageManager {

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun detectLanguageFromText(text: String): AppLanguage {
        // Detect Devanagari script (Hindi)
        if (text.any { it in '\u0900'..'\u097F' }) {
            return AppLanguage.HINDI
        }
        // Detect Bengali script
        if (text.any { it in '\u0980'..'\u09FF' }) {
            return AppLanguage.BENGALI
        }
        // Detect Hinglish / Latin script keywords
        val lower = text.lowercase()
        if (lower.contains("mujhe") || lower.contains("hai") || lower.contains("ho raha") || lower.contains("chakka")) {
            return AppLanguage.HINDI
        }
        if (lower.contains("amar") || lower.contains("matha") || lower.contains("ghurchhe")) {
            return AppLanguage.BENGALI
        }

        return _currentLanguage.value
    }
}
