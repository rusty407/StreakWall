package com.console.streakwall.wallpaper

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.console.streakwall.util.DateUtils
import java.util.concurrent.TimeUnit

/**
 * Owns all scheduling of [WallpaperUpdateWorker].
 *
 * The day number must change at local midnight, which a fixed 24h periodic request can't do:
 * its period is anchored to whenever it was first enqueued, and it drifts on 23h/25h DST days.
 * Instead this is a chain of one-time jobs: each run schedules the next one for the following
 * midnight, recomputing the delay from the current clock and timezone every time.
 */
object WallpaperScheduler {

    private const val DAILY_WORK_NAME = "wallpaper_midnight_update"

    // Land just after midnight, never just before it, so the worker always sees the new date.
    private const val MIDNIGHT_BUFFER_MILLIS = 30_000L

    /**
     * Ensures a midnight job is pending. Called on every app start and after boot, so it uses
     * KEEP: if a job is already waiting, its schedule is left untouched.
     */
    fun scheduleDaily(context: Context) = enqueue(context, ExistingWorkPolicy.KEEP)

    /**
     * Called by the worker itself to queue tomorrow's run. KEEP can't be used here (the
     * currently running job still counts as existing work) and REPLACE would cancel the running
     * job, so the new job is appended as its successor. Its delay is measured from when the
     * current run finishes, which is what we want.
     */
    fun scheduleNextMidnight(context: Context) = enqueue(context, ExistingWorkPolicy.APPEND_OR_REPLACE)

    private fun enqueue(context: Context, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<WallpaperUpdateWorker>()
            .setInitialDelay(DateUtils.millisUntilNextMidnight() + MIDNIGHT_BUFFER_MILLIS, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(DAILY_WORK_NAME, policy, request)
    }
}
