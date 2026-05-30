package com.example.schreibenaufdeutsch.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schreibenaufdeutsch.data.local.PreferenceManager
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    val selectedLanguage: StateFlow<String> = PreferenceManager.translationLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TranslateLanguage.ENGLISH)

    val isDeveloperMode: StateFlow<Boolean> = PreferenceManager.isDeveloperModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateTranslationLanguage(languageCode: String) {
        viewModelScope.launch {
            PreferenceManager.setTranslationLanguage(languageCode)
        }
    }

    fun toggleDeveloperMode() {
        viewModelScope.launch {
            val current = isDeveloperMode.value
            PreferenceManager.setDeveloperMode(!current)
        }
    }

    val availableLanguages = listOf(
        LanguageOption("English", TranslateLanguage.ENGLISH),
        LanguageOption("Spanish", TranslateLanguage.SPANISH),
        LanguageOption("French", TranslateLanguage.FRENCH),
        LanguageOption("Italian", TranslateLanguage.ITALIAN),
        LanguageOption("Turkish", TranslateLanguage.TURKISH),
        LanguageOption("Polish", TranslateLanguage.POLISH),
        LanguageOption("Russian", TranslateLanguage.RUSSIAN),
        LanguageOption("Ukrainian", TranslateLanguage.UKRAINIAN),
        LanguageOption("Albanian", "sq"),
        LanguageOption("Serbian", "sr"),
        LanguageOption("Arabic", TranslateLanguage.ARABIC)
    )
}

data class LanguageOption(val name: String, val code: String)
