/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.content.Context
import androidx.work.*
import org.emunix.insteadlauncher.helpers.network.RepoUpdater
import java.util.concurrent.TimeUnit

class UpdateRepositoryWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return if (RepoUpdater(applicationContext).update()) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}

object UpdateRepositoryWork {

        @JvmStatic
        fun start(context: Context) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val updateRequest =
                    PeriodicWorkRequest.Builder(UpdateRepositoryWorker::class.java, 1, TimeUnit.DAYS)
                            .setConstraints(constraints)
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, PeriodicWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                            .build()

            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork("update_repo_work", ExistingPeriodicWorkPolicy.KEEP ,updateRequest)
        }

        @JvmStatic
        fun stop(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("update_repo_work")
        }
}
