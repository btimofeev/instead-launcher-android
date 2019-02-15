/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.event

data class DownloadProgressEvent(
        val gameName: String,
        val bytesRead: Long,
        val contentLength: Long,
        val progressMessage: String,
        val done: Boolean = false,
        val error: Boolean = false,
        val errorMessage: String = ""
)