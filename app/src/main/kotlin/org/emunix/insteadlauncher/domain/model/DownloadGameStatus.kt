/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.model

sealed interface DownloadGameStatus {

    val gameName: String

    data class Downloading(
        override val gameName: String,
        val downloadedBytes: Long,
        val contentLength: Long,
    ) : DownloadGameStatus {

        val downloadedInPercentage: Int
            get() = (100 * downloadedBytes / contentLength).toInt()
    }

    class Success(override val gameName: String) : DownloadGameStatus

    class Error(
        override val gameName: String,
        val errorMessage: String
    ) : DownloadGameStatus
}
