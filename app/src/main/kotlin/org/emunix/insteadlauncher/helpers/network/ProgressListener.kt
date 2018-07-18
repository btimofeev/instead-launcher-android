package org.emunix.insteadlauncher.helpers.network

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}