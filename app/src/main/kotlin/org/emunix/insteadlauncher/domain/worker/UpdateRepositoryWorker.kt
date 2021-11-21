/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.worker

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS

/**
 * Worker for starting and stopping a background update of a repository
 */
interface UpdateRepositoryWorker {

    /**
     * Run the worker to periodically update the repository
     *
     * @param repeatInterval The repeat interval in [repeatIntervalTimeUnit] units
     * @param repeatIntervalTimeUnit The [TimeUnit] for [repeatInterval]
     */
    fun start(repeatInterval: Long = 1L, repeatIntervalTimeUnit: TimeUnit = DAYS)

    /**
     * Stop updating the repository
     */
    fun stop()
}