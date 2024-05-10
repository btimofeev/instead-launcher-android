/*
 * Copyright (c) 2023-2024 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.usecase.DeleteGameUseCase
import org.emunix.insteadlauncher.domain.work.DeleteGameWork
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import org.emunix.insteadlauncher.services.DeleteGameWorker.Companion.GAME_NAME_KEY
import org.emunix.insteadlauncher.utils.createForegroundInfoCompat
import javax.inject.Inject

@HiltWorker
class DeleteGameWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deleteGameUseCase: DeleteGameUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val gameName = inputData.getString(GAME_NAME_KEY) ?: return Result.failure()
        runCatching {
            deleteGameUseCase(gameName)
        }.onFailure {
            showErrorNotification()
            return Result.failure()
        }
        return Result.success()
    }

    @SuppressLint("InlinedApi")
    override suspend fun getForegroundInfo(): ForegroundInfo =
        createForegroundInfoCompat(
            notificationId = InsteadLauncher.UNINSTALL_NOTIFICATION_ID,
            notification = createNotification(),
            foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

    private fun createNotification(): Notification {
        val gameName = inputData.getString(GAME_NAME_KEY)
        val notificationIntent = Intent(appContext, LauncherActivity::class.java)
        notificationIntent.putExtra("game_name", gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = NavDeepLinkBuilder(appContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.gameFragment)
            .setArguments(bundleOf("game_name" to gameName))
            .createPendingIntent()

        return NotificationCompat.Builder(appContext, InsteadLauncher.CHANNEL_UNINSTALL)
            .setContentTitle(gameName)
            .setContentText(appContext.getText(R.string.notification_delete_game))
            .setSmallIcon(R.drawable.ic_delete_white_24dp)
            .setContentIntent(pendingIntent)
            .build()
    }

    private suspend fun showErrorNotification() = withContext(Dispatchers.Main) {
        Toast.makeText(appContext, R.string.error_failed_to_delete_file, Toast.LENGTH_LONG).show()
    }

    companion object {

        const val GAME_NAME_KEY = "GAME_NAME"
    }
}

class DeleteGameWorkImpl @Inject constructor(private val context: Context) : DeleteGameWork {

    override fun delete(gameName: String) {

        val deleteWork = OneTimeWorkRequestBuilder<DeleteGameWorker>()
            .setInputData(workDataOf(GAME_NAME_KEY to gameName))
            .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                /* uniqueWorkName = */ DELETE_GAME_NAME + gameName,
                /* existingWorkPolicy = */ REPLACE,
                /* work = */ deleteWork
            )
    }

    companion object {

        private const val DELETE_GAME_NAME = "delete_game: "
    }
}
