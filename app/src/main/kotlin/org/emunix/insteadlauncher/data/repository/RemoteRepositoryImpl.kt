/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.data.network.fetcher.GameListFetcher
import org.emunix.insteadlauncher.data.mapper.DownloadProgress
import org.emunix.insteadlauncher.data.parser.GameListParser
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import org.emunix.insteadlauncher.data.network.ProgressResponseBody
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class RemoteRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient.Builder,
    private val notificationRepository: NotificationRepository,
    private val preferencesProvider: PreferencesProvider,
    private val fetcher: GameListFetcher,
    private val parser: GameListParser,
) : RemoteRepository {

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun download(
        url: String,
        gameName: String,
    ): InputStream {
        val request = Request.Builder().url(url).build()
        val client = httpClient
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                val responseBody = originalResponse.body
                if (responseBody != null) {
                    originalResponse.newBuilder()
                        .body(ProgressResponseBody(responseBody) { downloadProgress ->
                            sendNotification(gameName, downloadProgress)
                        })
                        .build()
                } else {
                    originalResponse
                }
            }
            .build()
        val call = client.newCall(request)
        val job = currentCoroutineContext()[Job]
        job?.invokeOnCompletion(onCancelling = true) { call.cancel() }
        return suspendCancellableCoroutine { continuation ->
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!continuation.isCancelled) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body
                    if (!response.isSuccessful || responseBody == null) {
                        response.close()
                        if (!continuation.isCancelled) {
                            continuation.resumeWithException(IOException("Failed to download file"))
                        }
                    } else {
                        if (continuation.isCancelled) {
                            response.close()
                        } else {
                            continuation.resume(responseBody.byteStream())
                        }
                    }
                }
            })
        }
    }

    override suspend fun getGameList(): List<GameModel> = withContext(Dispatchers.IO) {
        val gamesMap = mutableMapOf<String, GameModel>()
        if (preferencesProvider.isSandboxEnabled) {
            gamesMap.putAll(parseXML(fetchXML(preferencesProvider.sandboxUrl)))
        }
        gamesMap.putAll(parseXML(fetchXML(preferencesProvider.repositoryUrl)))
        return@withContext gamesMap.values.toList()
    }

    private fun fetchXML(url: String): String = fetcher.fetch(url)

    private fun parseXML(xml: String): Map<String, GameModel> = parser.parse(xml)

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