/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.IOUtils
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_INSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.INSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.db.Game.State.*
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Error
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.helpers.*
import org.emunix.insteadlauncher.helpers.network.ProgressListener
import org.emunix.insteadlauncher.helpers.network.ProgressResponseBody
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipException
import javax.inject.Inject

@AndroidEntryPoint
class InstallGame : IntentService("InstallGame") {

    private lateinit var gameName: String
    private lateinit var gameTitle: String
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob)

    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var gamesDB: GameDao

    @Inject
    lateinit var gamesDbHelper: GameDbHelper

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var getDownloadGamesStatusUseCase: GetDownloadGamesStatusUseCase

    override fun onHandleIntent(intent: Intent?) {
        gameName = intent?.getStringExtra(EXTRA_GAME_NAME) ?: return
        gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE) ?: ""

        notificationBuilder = createNotification()
        startForeground(INSTALL_NOTIFICATION_ID, notificationBuilder.build())
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        observeDownloadStatus(gameName)

        val url = intent.getStringExtra(EXTRA_GAME_URL)
        val game = gamesDB.getByName(gameName)
        if (url != null) {
            try {
                gamesDbHelper.saveStateToDB(game, IS_INSTALL)
                val zipfile = File(storage.getCacheDirectory(), extractFilename(url))
                download(url, zipfile, gameName)

                val gameDir = File(storage.getGamesDirectory(), gameName)
                gameDir.deleteRecursively()
                zipfile.unzip(storage.getGamesDirectory())
                zipfile.deleteRecursively()
                gamesDbHelper.saveStateToDB(game, INSTALLED)
                gamesDbHelper.saveInstalledVersionToDB(game, game.version)
            } catch (e: IndexOutOfBoundsException) {
                // invalid url (exception from String.substring)
                publishErrorNotification("Bad url: $url")
                gamesDbHelper.saveStateToDB(game, NO_INSTALLED)
            } catch (e: IOException) {
                publishErrorNotification(e.localizedMessage ?: getString(R.string.error_failed_to_download_file, url))
                gamesDbHelper.saveStateToDB(game, NO_INSTALLED)
            } catch (e: ZipException) {
                publishErrorNotification(getString(R.string.error_failed_to_unpack_zip))
                gamesDbHelper.saveStateToDB(game, NO_INSTALLED)
            }
        }

        stopForeground(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun createNotification(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, LauncherActivity::class.java)
        notificationIntent.putExtra(EXTRA_GAME_NAME, gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.gameFragment)
            .setArguments(bundleOf(EXTRA_GAME_NAME to gameName))
            .createPendingIntent()

        return NotificationCompat.Builder(this, CHANNEL_INSTALL)
            .setContentTitle(gameTitle)
            .setContentText("0%")
            .setProgress(100, 0, false)
            .setSmallIcon(R.drawable.ic_download_white_24dp)
            .setContentIntent(pendingIntent)
    }

    @Throws(IOException::class)
    private fun download(url: String, file: File, gameName: String) {

        val progressListener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                serviceScope.launch {
                    val status = if (done) {
                        Success(gameName)
                    } else {
                        Downloading(
                            gameName = gameName,
                            downloadedBytes = bytesRead,
                            contentLength = contentLength,
                        )
                    }
                    notificationRepository.publishDownloadGameStatus(status)
                }
            }
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .body(ProgressResponseBody(originalResponse.body, progressListener))
                    .build()
            }
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val msg = application.getString(R.string.error_failed_to_download_file, url)
            throw IOException(msg)
        }
        FileOutputStream(file).use { toFile ->
            IOUtils.copy(response.body.byteStream(), toFile)
        }
    }

    private fun extractFilename(url: String): String {
        return url.substring(url.lastIndexOf('/') + 1)
    }

    private fun publishErrorNotification(message: String) = serviceScope.launch {
        notificationRepository.publishDownloadGameStatus(
            Error(
                gameName = gameName,
                errorMessage = message,
            )
        )
    }

    private fun observeDownloadStatus(gameName: String) = serviceScope.launch {
        getDownloadGamesStatusUseCase()
            .filter { it.gameName == gameName }
            .collect { downloadStatus ->
                when (downloadStatus) {
                    is Error -> {
                        NotificationHelper(this@InstallGame)
                            .showError(getString(R.string.error), downloadStatus.errorMessage, pendingIntent)
                    }
                    is Downloading -> {
                        if (downloadStatus.contentLength == CONTENT_LENGTH_UNAVAILABLE) {
                            notificationBuilder.setProgress(100, 0, true)
                                .setContentText(getText(R.string.notification_download_game))
                        } else {
                            val progress = downloadStatus.downloadedInPercentage
                            notificationBuilder.setProgress(100, progress, false)
                                .setContentText("$progress%")
                        }
                        notificationManager.notify(INSTALL_NOTIFICATION_ID, notificationBuilder.build())
                    }
                    is Success -> {
                        notificationBuilder.setProgress(100, 0, true)
                            .setContentText(getText(R.string.notification_install_game))
                        notificationManager.notify(INSTALL_NOTIFICATION_ID, notificationBuilder.build())
                    }
                }
            }
    }

    companion object {

        private const val CONTENT_LENGTH_UNAVAILABLE = -1L

        private const val EXTRA_GAME_NAME = "game_name"
        private const val EXTRA_GAME_URL = "game_url"
        private const val EXTRA_GAME_TITLE = "game_title"

        @JvmStatic
        fun start(context: Context, gameName: String, gameUrl: String, gameTitle: String) {
            val intent = Intent(context, InstallGame::class.java).apply {
                putExtra(EXTRA_GAME_NAME, gameName)
                putExtra(EXTRA_GAME_URL, gameUrl)
                putExtra(EXTRA_GAME_TITLE, gameTitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
