/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain

object DownloadConfig {

    const val MAX_CONCURRENT_DOWNLOADS = 5

    const val PROGRESS_EVENTS_BUFFER_CAPACITY = MAX_CONCURRENT_DOWNLOADS * 2
}