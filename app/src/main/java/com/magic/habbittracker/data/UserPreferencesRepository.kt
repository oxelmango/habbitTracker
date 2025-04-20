package com.magic.habbittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.magic.habbittracker.data.MotivationalPhrases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "user_preferences"
        )

        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val REMINDER_TIME_KEY = stringPreferencesKey("reminder_time")
        val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        val VERSION_CLICK_COUNT_KEY = stringPreferencesKey("version_click_count")
        val MOTIVATIONAL_PHRASE_KEY = stringPreferencesKey("motivational_phrase")
        val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")
        val HAS_COMPLETED_ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    val reminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_TIME_KEY] ?: "08:00"
    }
    
    val userNickname: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NICKNAME_KEY] ?: "Habit Master"
    }
    
    val versionClickCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[VERSION_CLICK_COUNT_KEY]?.toIntOrNull() ?: 0
    }
    
    val motivationalPhrase: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MOTIVATIONAL_PHRASE_KEY] ?: MotivationalPhrases.getRandomPhrase()
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_TIME_KEY] = time
        }
    }
    
    suspend fun setUserNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NICKNAME_KEY] = nickname
        }
    }
    
    suspend fun incrementVersionClickCount(): Int {
        val currentCount = versionClickCount.first()
        val newCount = currentCount + 1
        
        context.dataStore.edit { preferences ->
            preferences[VERSION_CLICK_COUNT_KEY] = newCount.toString()
        }
        
        return newCount
    }
    
    suspend fun resetVersionClickCount() {
        context.dataStore.edit { preferences ->
            preferences[VERSION_CLICK_COUNT_KEY] = "0"
        }
    }
    
    suspend fun updateMotivationalPhrase() {
        context.dataStore.edit { preferences ->
            preferences[MOTIVATIONAL_PHRASE_KEY] = MotivationalPhrases.getRandomPhrase()
        }
    }
    
    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_RUN_KEY] ?: true
    }
    
    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_RUN_KEY] = false
        }
    }
    
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_COMPLETED_ONBOARDING_KEY] ?: false
    }
    
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING_KEY] = true
        }
    }
    
    suspend fun resetAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}