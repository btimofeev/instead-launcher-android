/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.mapper

data class DownloadProgress(
    val bytesRead: Long,
    val contentLength: Long,
    val isDone: Boolean
)