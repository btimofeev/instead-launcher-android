/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import org.emunix.insteadlauncher.domain.worker.UpdateRepositoryWorker
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import javax.inject.Inject

/**
 * Start the worker to periodically update the repository
 */
interface StartUpdateRepositoryWorkUseCase {

    /**
     * Start the worker to periodically update the repository
     *
     * @param repeatInterval The repeat interval in [repeatIntervalTimeUnit] units
     * @param repeatIntervalTimeUnit The [TimeUnit] for [repeatInterval]
     */
    fun execute(repeatInterval: Long = 1L, repeatIntervalTimeUnit: TimeUnit = DAYS)
}

class StartUpdateRepositoryWorkUseCaseImpl @Inject constructor(
    private val updateRepositoryWorker: UpdateRepositoryWorker
) : StartUpdateRepositoryWorkUseCase {

    override fun execute(repeatInterval: Long, repeatIntervalTimeUnit: TimeUnit) {
        updateRepositoryWorker.start(
            repeatInterval = repeatInterval,
            repeatIntervalTimeUnit = repeatIntervalTimeUnit,
        )
    }
}