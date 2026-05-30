package com.example.schreibenaufdeutsch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.schreibenaufdeutsch.SchreibenApp
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferenceManager {
    private val TRANSLATION_LANGUAGE_KEY = stringPreferencesKey("translation_language")
    private val DAILY_TASK_COUNT_KEY = intPreferencesKey("daily_task_count")
    private val LAST_RESET_DATE_KEY = stringPreferencesKey("last_reset_date")
    private val IS_DEVELOPER_MODE_KEY = booleanPreferencesKey("is_developer_mode")

    val translationLanguageFlow: Flow<String> = SchreibenApp.instance.dataStore.data
        .map { preferences ->
            preferences[TRANSLATION_LANGUAGE_KEY] ?: TranslateLanguage.ENGLISH
        }

    val isDeveloperModeFlow: Flow<Boolean> = SchreibenApp.instance.dataStore.data
        .map { preferences ->
            preferences[IS_DEVELOPER_MODE_KEY] ?: false
        }

    suspend fun setTranslationLanguage(languageCode: String) {
        SchreibenApp.instance.dataStore.edit { preferences ->
            preferences[TRANSLATION_LANGUAGE_KEY] = languageCode
        }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        SchreibenApp.instance.dataStore.edit { preferences ->
            preferences[IS_DEVELOPER_MODE_KEY] = enabled
        }
    }

    /**
     * Checks if the user can generate a task today.
     * Resets the counter if the date has changed.
     */
    suspend fun canGenerateTask(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val preferences = SchreibenApp.instance.dataStore.data.first()
        
        val isDev = preferences[IS_DEVELOPER_MODE_KEY] ?: false
        if (isDev) return true // Developer bypass

        val lastResetDate = preferences[LAST_RESET_DATE_KEY] ?: ""
        val count = preferences[DAILY_TASK_COUNT_KEY] ?: 0

        if (today != lastResetDate) {
            // New day, reset count
            SchreibenApp.instance.dataStore.edit { prefs ->
                prefs[LAST_RESET_DATE_KEY] = today
                prefs[DAILY_TASK_COUNT_KEY] = 0
            }
            return true
        }

        return count < 5
    }

    suspend fun incrementDailyTaskCount() {
        SchreibenApp.instance.dataStore.edit { preferences ->
            val currentCount = preferences[DAILY_TASK_COUNT_KEY] ?: 0
            preferences[DAILY_TASK_COUNT_KEY] = currentCount + 1
        }
    }
}
