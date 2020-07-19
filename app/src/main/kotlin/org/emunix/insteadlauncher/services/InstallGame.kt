/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipException
import android.app.PendingIntent
import android.os.Build
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_INSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.INSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.data.Game.State.*
import org.emunix.insteadlauncher.event.DownloadProgressEvent
import org.emunix.insteadlauncher.helpers.*
import org.emunix.insteadlauncher.helpers.network.ProgressListener
import org.emunix.insteadlauncher.ui.game.GameActivity
import org.emunix.insteadlauncher.helpers.network.ProgressResponseBody
import org.emunix.insteadlauncher.storage.Storage
import javax.inject.Inject


class InstallGame : IntentService("InstallGame") {

    companion object {
        const val CONTENT_LENGTH_UNAVAILABLE = -1L

        @JvmStatic
        fun start(context: Context, gameName: String, gameUrl: String, gameTitle: String) {
            val intent = Intent(context, InstallGame::class.java).apply {
                putExtra("game_name", gameName)
                putExtra("game_url", gameUrl)
                putExtra("game_title", gameTitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private lateinit var gameName: String
    private lateinit var gameTitle: String
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject lateinit var storage: Storage

    override fun onHandleIntent(intent: Intent?) {
        gameName = intent?.getStringExtra("game_name") ?: return
        gameTitle = intent.getStringExtra("game_title")

        notificationBuilder = createNotification()
        startForeground(INSTALL_NOTIFICATION_ID, notificationBuilder.build())
        notificationManager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        InsteadLauncher.appComponent.inject(this)

        val url = intent.getStringExtra("game_url")
        val game = InsteadLauncher.db.games().getByName(gameName)
        if (url != null) {
            try {
                game.saveStateToDB(IS_INSTALL)
                val zipfile = File(storage.getCacheDirectory(), extractFilename(url))
                download(url, zipfile)

                notificationBuilder.setProgress(100, 0, true)
                        .setContentText(getText(R.string.notification_install_game))
                notificationManager.notify(INSTALL_NOTIFICATION_ID, notificationBuilder.build())

                val gameDir = File(storage.getGamesDirectory(), gameName)
                gameDir.deleteRecursively()
                zipfile.unzip(storage.getGamesDirectory())
                zipfile.deleteRecursively()
                game.saveStateToDB(INSTALLED)
                game.saveInstalledVersionToDB(game.version)
            } catch (e: IndexOutOfBoundsException) {
                // invalid url (exception from String.substring)
                NotificationHelper(this).showError(getString(R.string.error), "Bad url: $url", pendingIntent)
                game.saveStateToDB(NO_INSTALLED)
            } catch (e: IOException) {
                NotificationHelper(this).showError(getString(R.string.error), e.localizedMessage
                        ?: getString(R.string.error_failed_to_download_file, url), pendingIntent)
                game.saveStateToDB(NO_INSTALLED)
            } catch (e: ZipException) {
                NotificationHelper(this).showError(getString(R.string.error), getString(R.string.error_failed_to_unpack_zip), pendingIntent)
                game.saveStateToDB(NO_INSTALLED)
            }
        }

        stopForeground(true)
    }

    private fun createNotification(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, GameActivity::class.java)
        notificationIntent.putExtra("game_name", gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_INSTALL)
                .setContentTitle(gameTitle)
                .setContentText("0%")
                .setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_download_white_24dp)
                .setContentIntent(pendingIntent)
    }

    @Throws(IOException::class)
    private fun download(url: String, file: File) {

        val progressListener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val msg: String = application.getString(R.string.game_activity_message_downloading,
                        FileUtils.byteCountToDisplaySize(bytesRead),
                        if (contentLength == CONTENT_LENGTH_UNAVAILABLE) "??" else FileUtils.byteCountToDisplaySize(contentLength))

                var progress = -1
                if (contentLength == CONTENT_LENGTH_UNAVAILABLE) {
                    notificationBuilder.setProgress(100, 0, true)
                            .setContentText(getText(R.string.notification_download_game))
                } else {
                    progress = (100 * bytesRead / contentLength).toInt()
                    notificationBuilder.setProgress(100, progress, false)
                            .setContentText("$progress%")
                }
                notificationManager.notify(INSTALL_NOTIFICATION_ID, notificationBuilder.build())
                RxBus.publish(DownloadProgressEvent(gameName, progress, msg, done))
            }
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val originalResponse = chain.proceed(chain.request())
                    originalResponse.newBuilder()
                            .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                            .build()
                }
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val msg = application.getString(R.string.error_failed_to_download_file, url)
            RxBus.publish(DownloadProgressEvent(gameName, 0, "", true, true, msg))
            throw IOException(msg)
        }
        FileOutputStream(file).use { toFile ->
            IOUtils.copy(response.body?.byteStream(), toFile)
        }
    }

    private fun extractFilename(url: String): String {
        return url.substring(url.lastIndexOf('/') + 1)
    }
}
