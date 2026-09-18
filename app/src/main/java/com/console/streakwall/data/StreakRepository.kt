package com.console.streakwall.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.console.streakwall.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "streakwall_preferences")

/**
 * Single source of truth for all persisted app state. This is the only place that touches
 * DataStore directly; ViewModels depend on this, not on Context/DataStore themselves, so
 * the storage mechanism can change later without touching UI code.
 */
class StreakRepository(private val context: Context) {

    private object Keys {
        val HABIT_NAME = stringPreferencesKey("habit_name")
        val START_DATE_MILLIS = longPreferencesKey("start_date_millis")
        val THEME = stringPreferencesKey("theme")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    }

    val preferencesFlow: Flow<StreakPreferences> = context.dataStore.data.map { prefs ->
        StreakPreferences(
            habitName = prefs[Keys.HABIT_NAME] ?: "",
            startDateMillis = prefs[Keys.START_DATE_MILLIS] ?: DateUtils.todayStartOfDayMillis(),
            theme = WallpaperTheme.fromName(prefs[Keys.THEME]),
            isOnboarded = prefs[Keys.IS_ONBOARDED] ?: false
        )
    }

    suspend fun completeOnboarding(habitName: String, startDateMillis: Long, theme: WallpaperTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HABIT_NAME] = habitName
            prefs[Keys.START_DATE_MILLIS] = startDateMillis
            prefs[Keys.THEME] = theme.name
            prefs[Keys.IS_ONBOARDED] = true
        }
    }

    suspend fun updateHabitName(habitName: String) {
        context.dataStore.edit { prefs -> prefs[Keys.HABIT_NAME] = habitName }
    }

    suspend fun updateTheme(theme: WallpaperTheme) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME] = theme.name }
    }

    /** Resets the streak to start counting from today. Irreversible — callers must confirm. */
    suspend fun resetStreak() {
        context.dataStore.edit { prefs ->
            prefs[Keys.START_DATE_MILLIS] = DateUtils.todayStartOfDayMillis()
        }
    }
}
