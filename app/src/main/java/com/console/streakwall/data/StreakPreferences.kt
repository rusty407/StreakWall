package com.console.streakwall.data

import com.console.streakwall.util.DateUtils

/**
 * Snapshot of everything StreakWall persists, all local-only (DataStore Preferences on this
 * device — see StreakRepository). There is no backend and no account: each install's data
 * lives entirely in its own app-private storage, which is what "local-only, multi-user" in
 * the sense of "any number of independent installs" means here — there is no in-app
 * multi-profile switcher, since nothing in this app's scope needs one.
 */
data class StreakPreferences(
    val habitName: String,
    val startDateMillis: Long,
    val theme: WallpaperTheme,
    val isOnboarded: Boolean
) {
    val dayNumber: Int get() = DateUtils.dayNumber(startDateMillis)

    companion object {
        val Empty = StreakPreferences(
            habitName = "",
            startDateMillis = DateUtils.todayStartOfDayMillis(),
            theme = WallpaperTheme.Default,
            isOnboarded = false
        )
    }
}
