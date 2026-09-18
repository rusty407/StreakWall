package com.console.streakwall.wallpaper

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.console.streakwall.StreakWallApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Runs just after local midnight (see [WallpaperScheduler]) to redraw the wallpaper so the day
 * count advances without the user opening the app, then queues the next night's run. A
 * [CoroutineWorker] is used because reading the current streak needs a suspending DataStore read.
 *
 * Before onboarding there is no habit/start date to render, so the wallpaper step is skipped,
 * but the next run is still queued.
 */
class WallpaperUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val app = applicationContext as StreakWallApplication
            val preferences = app.repository.preferencesFlow.first()

            // update() returns false when the platform forbids wallpaper changes (e.g. a managed
            // device). That is permanent, so it is not retried.
            if (preferences.isOnboarded) {
                WallpaperUpdater.update(applicationContext, preferences)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Usually transient (low memory, launcher busy): retry with backoff a few times.
            // The next run must not be queued yet, or a retry would leave two chains running.
            if (runAttemptCount < MAX_ATTEMPTS) return Result.retry()
        }

        WallpaperScheduler.scheduleNextMidnight(applicationContext)
        return Result.success()
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
