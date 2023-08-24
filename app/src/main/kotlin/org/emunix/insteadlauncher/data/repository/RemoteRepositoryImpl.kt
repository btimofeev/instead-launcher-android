/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.emunix.insteadlauncher.data.mapper.DownloadProgress
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import org.emunix.insteadlauncher.helpers.network.ProgressResponseBody
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class RemoteRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient.Builder,
    private val notificationRepository: NotificationRepository,
) : RemoteRepository {

    override suspend fun download(
        url: String,
        gameName: String,
    ): InputStream = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val client = httpClient
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .body(ProgressResponseBody(originalResponse.body) { downloadProgress ->
                        sendNotification(gameName, downloadProgress)
                    })
                    .build()
            }
            .build()
        val response = async { client.newCall(request).execute() }.await()
        if (!response.isSuccessful) {
            throw IOException("Failed to download file")
        }
        return@withContext response.body.byteStream()
    }

    private fun sendNotification(gameName: String, downloadProgress: DownloadProgress) {
        val status = if (downloadProgress.isDone) {
            Success(gameName)
        } else {
            Downloading(
                gameName = gameName,
                downloadedBytes = downloadProgress.bytesRead,
                contentLength = downloadProgress.contentLength,
            )
        }
        notificationRepository.publishDownloadGameStatus(status)
    }
}