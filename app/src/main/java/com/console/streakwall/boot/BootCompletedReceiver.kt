package com.console.streakwall.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.console.streakwall.wallpaper.WallpaperScheduler

/**
 * Re-arms the daily wallpaper job after a device reboot.
 *
 * WorkManager persists pending work across reboots on its own in the common case, but a
 * few OEM battery-optimization skins are known to interfere with that internal
 * rescheduling. Calling [WallpaperScheduler.scheduleDaily] here is cheap insurance: it uses
 * ExistingWorkPolicy.KEEP internally, so if the job is already correctly scheduled
 * this is a harmless no-op, and if it isn't, this puts it back.
 *
 * BroadcastReceivers get a short (~10s) execution window and cannot do async work directly,
 * so this only enqueues WorkManager work — it does not touch the wallpaper itself.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        WallpaperScheduler.scheduleDaily(context)
    }
}
