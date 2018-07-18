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