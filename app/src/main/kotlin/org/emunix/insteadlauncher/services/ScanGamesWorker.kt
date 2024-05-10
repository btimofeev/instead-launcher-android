/*
 * Copyright (c) 2023-2024 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCase
import org.emunix.insteadlauncher.domain.work.ScanGamesWork
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import org.emunix.insteadlauncher.utils.createForegroundInfoCompat
import javax.inject.Inject

@HiltWorker
class ScanGamesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scanAndUpdateLocalGamesUseCase: ScanAndUpdateLocalGamesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        runCatching {
            scanAndUpdateLocalGamesUseCase()
        }.onFailure {
            return Result.failure()
        }
        return Result.success()
    }

    @SuppressLint("InlinedApi")
    override suspend fun getForegroundInfo(): ForegroundInfo =
        createForegroundInfoCompat(
            notificationId = InsteadLauncher.SCAN_GAMES_NOTIFICATION_ID,
            notification = createNotification(),
            foregroundServiceType = FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

    private fun createNotification(): Notification {
        val notificationIntent = Intent(appContext, LauncherActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val flags = if (VERSION.SDK_INT >= VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            notificationIntent,
            flags
        )

        return NotificationCompat.Builder(appContext, InsteadLauncher.CHANNEL_SCAN_GAMES)
            .setContentTitle(appContext.getText(R.string.app_name))
            .setContentText(appContext.getText(R.string.notification_scan_games))
            .setSmallIcon(R.drawable.ic_search_24dp)
            .setContentIntent(pendingIntent)
            .build()
    }
}

class ScanGamesWorkImpl @Inject constructor(private val context: Context) : ScanGamesWork {

    override fun scan() {

        val scanWork = OneTimeWorkRequestBuilder<ScanGamesWorker>()
            .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                /* uniqueWorkName = */ SCAN_GAMES,
                /* existingWorkPolicy = */ REPLACE,
                /* work = */ scanWork
            )
    }

    companion object {

        private const val SCAN_GAMES = "scan_games"
    }
}
