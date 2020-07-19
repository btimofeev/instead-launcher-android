/*
 * Copyright (c) 2019-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.content.Context
import androidx.work.*
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.helpers.network.RepoUpdater
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateRepositoryWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    @Inject lateinit var repoUpdater: RepoUpdater

    override fun doWork(): Result {
        InsteadLauncher.appComponent.inject(this)

        return when (repoUpdater.update()) {
            true  -> Result.success()
            false -> Result.retry()
        }
    }
}

object UpdateRepositoryWork {

    private const val UPDATE_REPO_WORK = "update_repo_work"

    @JvmStatic
    fun start(context: Context) {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

        val updateRequest =
                PeriodicWorkRequest.Builder(UpdateRepositoryWorker::class.java, 1L, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, PeriodicWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                        .build()

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UPDATE_REPO_WORK, ExistingPeriodicWorkPolicy.KEEP, updateRequest)
    }

    @JvmStatic
    fun stop(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UPDATE_REPO_WORK)
    }
}
