/*
 * Copyright (c) 2018-2021, 2023, 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_INSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.INSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.DownloadConfig
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Error
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.InstallGameResult
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.DOWNLOAD_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.GAME_NOT_FOUND_IN_DATABASE
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.UNPACKING_ERROR
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.NotificationRepository
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.domain.usecase.InstallGameUseCase
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import org.emunix.insteadlauncher.presentation.navigation.GAME_INFO_SCREEN_DEEPLINK
import org.emunix.insteadlauncher.utils.NotificationHelper
import org.emunix.insteadlauncher.utils.writeToLog
import javax.inject.Inject

// TODO Рассмотреть вариант замены Service на WorkManager

@AndroidEntryPoint
class InstallGame : Service() {

    private data class DownloadRequest(
        val gameName: String,
        val gameTitle: String,
        val url: String,
        val originalState: GameState,
    )

    private val pendingRequests = ArrayDeque<DownloadRequest>()
    private val activeRequests = mutableMapOf<String, DownloadRequest>()
    private val activeJobs = mutableMapOf<String, Job>()
    private val downloadStatuses = mutableMapOf<String, DownloadGameStatus>()

    private var foregroundStarted = false
    private var notificationGameName = ""

    private lateinit var notificationManager: NotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var dataBaseRepository: DataBaseRepository

    @Inject
    lateinit var getDownloadGamesStatusUseCase: GetDownloadGamesStatusUseCase

    @Inject
    lateinit var installGameUseCase: InstallGameUseCase

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        observeDownloads()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> intent.getStringExtra(EXTRA_GAME_NAME)?.let { cancelDownload(it) }
            else -> {
                val request = intent?.let {
                    DownloadRequest(
                        gameName = it.getStringExtra(EXTRA_GAME_NAME) ?: return@let null,
                        gameTitle = it.getStringExtra(EXTRA_GAME_TITLE) ?: "",
                        url = it.getStringExtra(EXTRA_GAME_URL) ?: "",
                        originalState = GameState.valueOf(
                            it.getStringExtra(EXTRA_GAME_STATE) ?: GameState.NO_INSTALLED.name
                        )
                    )
                } ?: return START_NOT_STICKY
                if (isQueued(request.gameName)) return START_NOT_STICKY
                pendingRequests.addLast(request)
                notificationGameName = request.gameName
            }
        }
        startDownloads()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun isQueued(gameName: String): Boolean {
        return activeJobs.containsKey(gameName) || pendingRequests.any { it.gameName == gameName }
    }

    private fun startDownloads() {
        while (activeJobs.size < DownloadConfig.MAX_CONCURRENT_DOWNLOADS && pendingRequests.isNotEmpty()) {
            startDownload(pendingRequests.removeFirst())
        }
        updateForegroundNotification()
    }

    private fun startDownload(request: DownloadRequest) {
        activeRequests[request.gameName] = request
        activeJobs[request.gameName] = serviceScope.launch {
            runCatching { installGameUseCase(request.gameName, request.originalState) }
                .onSuccess { result ->
                    when (result) {
                        is InstallGameResult.Error -> handleError(request, result)
                        is InstallGameResult.Success -> Unit
                    }
                }
                .onFailure { throwable ->
                    if (throwable !is CancellationException) {
                        throwable.writeToLog()
                    }
                }
            activeJobs.remove(request.gameName)
            activeRequests.remove(request.gameName)
            downloadStatuses.remove(request.gameName)
            startDownloads()
        }
    }

    private fun cancelDownload(gameName: String) {
        val cancelledActive = activeJobs.containsKey(gameName)
        activeJobs[gameName]?.cancel()
        val pendingRequest = pendingRequests.firstOrNull { it.gameName == gameName }
        if (pendingRequest != null) {
            pendingRequests.remove(pendingRequest)
            restoreOriginalState(pendingRequest)
        }
        if (cancelledActive || pendingRequest != null) {
            startDownloads()
        }
    }

    private fun restoreOriginalState(request: DownloadRequest) {
        serviceScope.launch {
            withContext(NonCancellable) {
                dataBaseRepository.getGame(request.gameName)?.let { game ->
                    dataBaseRepository.updateGame(game.copy(state = request.originalState))
                }
            }
        }
    }

    private fun handleError(request: DownloadRequest, error: InstallGameResult.Error) {
        error.throwable?.writeToLog()
        val errorText = when (error.type) {
            DOWNLOAD_ERROR -> getString(R.string.error_failed_to_download_file, request.url)
            UNPACKING_ERROR -> getString(R.string.error_failed_to_unpack_zip)
            GAME_NOT_FOUND_IN_DATABASE -> getString(R.string.error)
        }
        publishErrorNotification(request.gameName, errorText)
    }

    private fun publishErrorNotification(gameName: String, message: String) = serviceScope.launch {
        notificationRepository.publishDownloadGameStatus(
            Error(
                gameName = gameName,
                errorMessage = message,
            )
        )
    }

    private fun observeDownloads() = serviceScope.launch {
        getDownloadGamesStatusUseCase()
            .collect { downloadStatus ->
                downloadStatuses[downloadStatus.gameName] = downloadStatus
                when (downloadStatus) {
                    is Error -> {
                        NotificationHelper(this@InstallGame)
                            .showError(
                                getString(R.string.error),
                                downloadStatus.errorMessage,
                                pendingIntentFor(downloadStatus.gameName)
                            )
                    }
                    is Downloading, is Success -> Unit
                }
                updateForegroundNotification()
            }
    }

    private fun updateForegroundNotification() {
        if (activeJobs.isEmpty() && pendingRequests.isEmpty()) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        val notification = buildNotification().build()
        if (!foregroundStarted) {
            startForeground(INSTALL_NOTIFICATION_ID, notification)
            foregroundStarted = true
        } else {
            notificationManager.notify(INSTALL_NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): NotificationCompat.Builder {
        val displayRequest =
            activeRequests[notificationGameName]
                ?: activeRequests.values.firstOrNull()
                ?: pendingRequests.first()

        val builder = NotificationCompat.Builder(this, CHANNEL_INSTALL)
            .setSmallIcon(R.drawable.ic_download_white_24dp)
            .setContentIntent(pendingIntentFor(displayRequest.gameName))

        val totalCount = activeJobs.size + pendingRequests.size
        if (totalCount == 1) {
            builder.setContentTitle(displayRequest.gameTitle)
        } else {
            builder.setContentTitle(getString(R.string.notification_download_games, totalCount))
        }

        val activeLines = activeRequests.values.map { request ->
            when (val status = downloadStatuses[request.gameName]) {
                is Downloading -> when {
                    status.contentLength == CONTENT_LENGTH_UNAVAILABLE ->
                        "${request.gameTitle}: ${getString(R.string.notification_download_game)}"
                    else -> "${request.gameTitle}: ${status.downloadedInPercentage}%"
                }
                is Success -> "${request.gameTitle}: ${getString(R.string.notification_install_game)}"
                else -> request.gameTitle
            }
        }
        val pendingLine =
            if (pendingRequests.isNotEmpty()) {
                getString(R.string.notification_games_in_queue, pendingRequests.size)
            } else {
                null
            }
        val lines = activeLines + listOfNotNull(pendingLine)
        val style = NotificationCompat.InboxStyle()
        lines.forEach { style.addLine(it) }
        builder.setContentText(lines.joinToString("\n"))
            .setStyle(style)
        return builder
    }

    private fun pendingIntentFor(gameName: String): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "$GAME_INFO_SCREEN_DEEPLINK/$gameName".toUri(),
            this,
            LauncherActivity::class.java
        )
        return PendingIntent.getActivity(
            this,
            gameName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {

        private const val CONTENT_LENGTH_UNAVAILABLE = -1L

        private const val ACTION_CANCEL = "org.emunix.insteadlauncher.services.InstallGame.CANCEL"

        private const val EXTRA_GAME_NAME = "game_name"
        private const val EXTRA_GAME_URL = "game_url"
        private const val EXTRA_GAME_TITLE = "game_title"
        private const val EXTRA_GAME_STATE = "game_state"

        @JvmStatic
        fun start(
            context: Context,
            gameName: String,
            gameUrl: String,
            gameTitle: String,
            originalState: GameState,
        ) {
            val intent = Intent(context, InstallGame::class.java).apply {
                putExtra(EXTRA_GAME_NAME, gameName)
                putExtra(EXTRA_GAME_URL, gameUrl)
                putExtra(EXTRA_GAME_TITLE, gameTitle)
                putExtra(EXTRA_GAME_STATE, originalState.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun cancel(context: Context, gameName: String) {
            val intent = Intent(context, InstallGame::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_GAME_NAME, gameName)
            }
            context.startService(intent)
        }
    }
}