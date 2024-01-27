/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

data class DownloadState(
    val progress: Int,
    val message: String,
)

data class DownloadError(
    val message: String,
)
