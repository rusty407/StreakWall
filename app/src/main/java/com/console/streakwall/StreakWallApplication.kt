package com.console.streakwall

import android.app.Application
import com.console.streakwall.data.StreakRepository
import com.console.streakwall.wallpaper.WallpaperScheduler

class StreakWallApplication : Application() {

    /** Simple manual DI: one repository instance shared by every ViewModel and the Worker. */
    val repository: StreakRepository by lazy { StreakRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // Safe to call unconditionally: KEEP (inside WallpaperScheduler) makes this a
        // no-op while a midnight job is already pending.
        WallpaperScheduler.scheduleDaily(this)
    }
}
