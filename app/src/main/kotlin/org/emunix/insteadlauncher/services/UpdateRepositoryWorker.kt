/*
 * Copyright (c) 2019-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.emunix.insteadlauncher.helpers.network.RepoUpdater
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import javax.inject.Inject

@HiltWorker
class UpdateRepositoryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repoUpdater: RepoUpdater
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return when (repoUpdater.update()) {
            true -> Result.success()
            false -> Result.retry()
        }
    }
}

/**
 * A class for starting and stopping a background update of a repository
 */
class UpdateRepositoryWorkManager @Inject constructor(private val context: Context) {

    /**
     * Run the work manager to periodically update the repository
     *
     * @param repeatInterval The repeat interval in [repeatIntervalTimeUnit] units
     * @param repeatIntervalTimeUnit The [TimeUnit] for [repeatInterval]
     */
    fun start(repeatInterval: Long = 1L, repeatIntervalTimeUnit: TimeUnit = DAYS) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val updateRequest =
            PeriodicWorkRequest.Builder(UpdateRepositoryWorker::class.java, repeatInterval, repeatIntervalTimeUnit)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    PeriodicWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UPDATE_REPO_WORK, ExistingPeriodicWorkPolicy.KEEP, updateRequest)
    }

    /**
     * Stop updating the repository
     */
    fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(UPDATE_REPO_WORK)
    }

    companion object {

        private const val UPDATE_REPO_WORK = "update_repo_work"
    }
}
