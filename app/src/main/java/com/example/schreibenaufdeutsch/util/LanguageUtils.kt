package com.example.schreibenaufdeutsch.util

import com.google.mlkit.nl.translate.TranslateLanguage

object LanguageUtils {
    fun getLanguageName(code: String): String {
        return when (code) {
            TranslateLanguage.ENGLISH -> "English"
            TranslateLanguage.SPANISH -> "Spanish"
            TranslateLanguage.FRENCH -> "French"
            TranslateLanguage.ITALIAN -> "Italian"
            TranslateLanguage.TURKISH -> "Turkish"
            TranslateLanguage.POLISH -> "Polish"
            TranslateLanguage.RUSSIAN -> "Russian"
            TranslateLanguage.UKRAINIAN -> "Ukrainian"
            "sq" -> "Albanian"
            "sr" -> "Serbian"
            TranslateLanguage.ARABIC -> "Arabic"
            else -> "English"
        }
    }

    fun splitIntoSentences(text: String): List<String> {
        // Split by newline OR by punctuation followed by space
        return text.split(Regex("(?<=[.!?])\\s+|\\n+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }
}
