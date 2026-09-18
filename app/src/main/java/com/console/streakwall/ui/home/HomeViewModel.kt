package com.console.streakwall.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.console.streakwall.data.StreakPreferences
import com.console.streakwall.data.StreakRepository
import com.console.streakwall.wallpaper.WallpaperScheduler
import com.console.streakwall.wallpaper.WallpaperUpdater
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface WallpaperRefreshEvent {
    data object Success : WallpaperRefreshEvent
    data class Failure(val message: String) : WallpaperRefreshEvent
}

/**
 * [appContext] is always the application context (see how this is constructed in
 * ui/ViewModelFactory usage) — never an Activity context, so it can't leak one.
 */
class HomeViewModel(
    private val appContext: Context,
    private val repository: StreakRepository
) : ViewModel() {

    val preferences: StateFlow<StreakPreferences?> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _refreshEvent = MutableStateFlow<WallpaperRefreshEvent?>(null)
    val refreshEvent: StateFlow<WallpaperRefreshEvent?> = _refreshEvent.asStateFlow()

    fun refreshWallpaperNow() {
        viewModelScope.launch { applyWallpaper() }
    }

    fun consumeRefreshEvent() {
        _refreshEvent.value = null
    }

    fun resetStreak() {
        viewModelScope.launch {
            repository.resetStreak()
            applyWallpaper()
            WallpaperScheduler.scheduleDaily(appContext)
        }
    }

    // Reads straight from the repository rather than the cached [preferences] StateFlow: right
    // after resetStreak() that cache can still hold the old start date, which would redraw the
    // wallpaper with the pre-reset day number.
    private suspend fun applyWallpaper() {
        _refreshEvent.value = try {
            val current = repository.preferencesFlow.first()
            val applied = withContext(Dispatchers.Default) {
                WallpaperUpdater.update(appContext, current)
            }
            if (applied) {
                WallpaperRefreshEvent.Success
            } else {
                WallpaperRefreshEvent.Failure("Wallpaper changes are restricted on this device")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Without this, a bitmap/WallpaperManager failure would crash the app from a button tap.
            WallpaperRefreshEvent.Failure(e.message ?: "Unknown error")
        }
    }
}
