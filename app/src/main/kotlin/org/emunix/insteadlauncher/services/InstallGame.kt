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
import kotlinx.coroutines.runBlocking
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_INSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.INSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Error
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.model.InstallGameResult
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.DOWNLOAD_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.GAME_NOT_FOUND_IN_DATABASE
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.UNPACKING_ERROR
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.domain.usecase.InstallGameUseCase
import org.emunix.insteadlauncher.utils.NotificationHelper
import org.emunix.insteadlauncher.utils.writeToLog
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import javax.inject.Inject

// TODO Рассмотреть вариант замены IntentService на WorkManager

@AndroidEntryPoint
class InstallGame : IntentService("InstallGame") {

    private lateinit var gameName: String
    private lateinit var gameTitle: String
    private lateinit var url: String
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob)

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var getDownloadGamesStatusUseCase: GetDownloadGamesStatusUseCase

    @Inject
    lateinit var installGameUseCase: InstallGameUseCase

    override fun onHandleIntent(intent: Intent?) {
        gameName = intent?.getStringExtra(EXTRA_GAME_NAME) ?: return
        gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE) ?: ""
        url = intent.getStringExtra(EXTRA_GAME_URL) ?: ""

        notificationBuilder = createNotification()
        startForeground(INSTALL_NOTIFICATION_ID, notificationBuilder.build())
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        observeDownloadStatus(gameName)

        runBlocking {
            runCatching { installGameUseCase(gameName) }
                .onSuccess { result ->
                    when (result) {
                        is InstallGameResult.Error -> handleError(result)
                        is InstallGameResult.Success -> Unit
                    }
                }
        }

        stopForeground(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun handleError(error: InstallGameResult.Error) {
        error.throwable?.writeToLog()
        val errorText = when (error.type) {
            DOWNLOAD_ERROR -> getString(R.string.error_failed_to_download_file, url)
            UNPACKING_ERROR -> getString(R.string.error_failed_to_unpack_zip)
            GAME_NOT_FOUND_IN_DATABASE -> getString(R.string.error)
        }
        publishErrorNotification(errorText)
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
