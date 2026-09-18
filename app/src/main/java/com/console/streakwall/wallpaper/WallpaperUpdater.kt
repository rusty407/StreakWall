package com.console.streakwall.wallpaper

import android.app.WallpaperManager
import android.content.Context
import com.console.streakwall.data.StreakPreferences

/**
 * Generates the current day's wallpaper bitmap and pushes it to both the home screen and
 * the lock screen via [WallpaperManager]. Shared by [WallpaperUpdateWorker] (the daily
 * background job) and the Home/Settings screens' manual "refresh now" actions, so there is
 * exactly one code path that talks to WallpaperManager.
 */
object WallpaperUpdater {

    /** @return true if the wallpaper was applied, false if the platform refused to allow it. */
    fun update(context: Context, preferences: StreakPreferences): Boolean {
        val wallpaperManager = WallpaperManager.getInstance(context)

        // isSetWallpaperAllowed is false under a handful of MDM/enterprise device-owner
        // restrictions; setBitmap would otherwise throw a SecurityException there.
        if (!wallpaperManager.isSetWallpaperAllowed) return false

        val bitmap = WallpaperGenerator.generate(
            context = context,
            dayNumber = preferences.dayNumber,
            habitName = preferences.habitName,
            theme = preferences.theme
        )

        // FLAG_SYSTEM = home screen, FLAG_LOCK = lock screen. Setting them separately (rather
        // than relying on FLAG_SYSTEM implicitly covering the lock screen) keeps this correct
        // on devices where the user has already set a distinct lock screen wallpaper.
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)

        bitmap.recycle()
        return true
    }
}
