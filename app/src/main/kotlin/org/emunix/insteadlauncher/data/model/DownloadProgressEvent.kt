/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.model

data class DownloadProgressEvent(
    val gameName: String,
    val progressValue: Int,
    val progressMessage: String,
    val done: Boolean = false,
    val error: Boolean = false,
    val errorMessage: String = ""
)