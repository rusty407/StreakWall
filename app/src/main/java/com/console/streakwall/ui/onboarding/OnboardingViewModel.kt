package com.console.streakwall.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.console.streakwall.data.StreakRepository
import com.console.streakwall.data.WallpaperTheme
import com.console.streakwall.util.DateUtils
import com.console.streakwall.wallpaper.WallpaperUpdater
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OnboardingUiState(
    val habitName: String = "",
    val startDateMillis: Long = DateUtils.todayStartOfDayMillis(),
    val theme: WallpaperTheme = WallpaperTheme.Default,
    val showValidationError: Boolean = false,
    val isComplete: Boolean = false
)

class OnboardingViewModel(
    private val appContext: Context,
    private val repository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun onHabitNameChanged(name: String) {
        _uiState.update { it.copy(habitName = name, showValidationError = false) }
    }

    fun onStartDateChanged(dateMillis: Long) {
        _uiState.update { it.copy(startDateMillis = dateMillis) }
    }

    fun onThemeChanged(theme: WallpaperTheme) {
        _uiState.update { it.copy(theme = theme) }
    }

    fun onContinueClicked() {
        val state = _uiState.value
        if (state.habitName.isBlank()) {
            _uiState.update { it.copy(showValidationError = true) }
            return
        }

        viewModelScope.launch {
            repository.completeOnboarding(
                habitName = state.habitName.trim(),
                startDateMillis = state.startDateMillis,
                theme = state.theme
            )
            // Draw the first wallpaper right away; otherwise it would stay stale until the next
            // daily job. A failure here (e.g. restricted device) must not block onboarding.
            try {
                withContext(Dispatchers.Default) {
                    WallpaperUpdater.update(appContext, repository.preferencesFlow.first())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ignored: the user can retry via "Refresh Wallpaper Now".
            }
            _uiState.update { it.copy(isComplete = true) }
        }
    }
}
