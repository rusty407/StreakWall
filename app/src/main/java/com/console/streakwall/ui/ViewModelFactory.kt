package com.console.streakwall.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.console.streakwall.data.StreakRepository

/**
 * Minimal manual-DI factory. The app is small enough that a full DI framework (Hilt, etc.)
 * would add more ceremony than it saves — every ViewModel takes at most two dependencies:
 * the repository, and the application [Context] (never an Activity context, so nothing can
 * leak one), both of which come from [com.console.streakwall.StreakWallApplication].
 */
class ViewModelFactory(
    private val appContext: Context,
    private val repository: StreakRepository,
    private val create: (Context, StreakRepository) -> ViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return create(appContext, repository) as T
    }
}
