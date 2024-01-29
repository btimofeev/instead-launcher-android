/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCase
import org.emunix.insteadlauncher.domain.work.ScanGamesWork
import javax.inject.Inject

@HiltWorker
class ScanGamesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
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
