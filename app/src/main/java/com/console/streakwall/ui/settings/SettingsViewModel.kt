package com.console.streakwall.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.console.streakwall.data.StreakPreferences
import com.console.streakwall.data.StreakRepository
import com.console.streakwall.data.WallpaperTheme
import com.console.streakwall.wallpaper.WallpaperUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val appContext: Context,
    private val repository: StreakRepository
) : ViewModel() {

    val preferences: StateFlow<StreakPreferences?> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _saveConfirmed = MutableStateFlow(false)
    val saveConfirmed: StateFlow<Boolean> = _saveConfirmed

    fun saveHabitName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateHabitName(name.trim())
            reapplyWallpaper()
            _saveConfirmed.value = true
        }
    }

    fun selectTheme(theme: WallpaperTheme) {
        viewModelScope.launch {
            repository.updateTheme(theme)
            reapplyWallpaper()
        }
    }

    fun reapplyWallpaperNow() {
        viewModelScope.launch { reapplyWallpaper() }
    }

    fun consumeSaveConfirmation() {
        _saveConfirmed.value = false
    }

    private suspend fun reapplyWallpaper() {
        // Read fresh from the repository rather than the cached preferences StateFlow: that
        // flow is only warm once the UI has started collecting it, so relying on it here
        // right after a write could race against an unpopulated cache.
        val current = repository.preferencesFlow.first()
        withContext(Dispatchers.Default) {
            WallpaperUpdater.update(appContext, current)
        }
    }
}
