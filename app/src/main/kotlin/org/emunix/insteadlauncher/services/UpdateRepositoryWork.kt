/*
 * Copyright (c) 2019-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Error
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Success
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCase
import org.emunix.insteadlauncher.domain.worker.UpdateRepositoryWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class UpdateRepositoryWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateGameListUseCase: UpdateGameListUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return when (updateGameListUseCase()) {
            is Success -> Result.success()
            is Error -> Result.retry()
        }
    }
}

class UpdateRepositoryWorkManager @Inject constructor(private val context: Context) : UpdateRepositoryWorker {

    override fun start(repeatInterval: Long, repeatIntervalTimeUnit: TimeUnit) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val updateRequest =
            PeriodicWorkRequest.Builder(UpdateRepositoryWork::class.java, repeatInterval, repeatIntervalTimeUnit)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UPDATE_REPO_WORK, ExistingPeriodicWorkPolicy.KEEP, updateRequest)
    }

    override fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(UPDATE_REPO_WORK)
    }

    companion object {

        private const val UPDATE_REPO_WORK = "update_repo_work"
    }
}
