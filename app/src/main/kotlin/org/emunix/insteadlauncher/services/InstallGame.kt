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
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_INSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.INSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.data.Game.State.*
import org.emunix.insteadlauncher.helpers.*
import org.emunix.insteadlauncher.ui.game.GameActivity


class InstallGame: IntentService("InstallGame") {

    override fun onHandleIntent(intent: Intent?) {
        val url = intent?.getStringExtra("game_url")
        val gameName = intent?.getStringExtra("game_name") ?: return
        val game = InsteadLauncher.gamesDB.gameDao().getGameByName(gameName)

        val notificationIntent = Intent(this, GameActivity::class.java)
        notificationIntent.putExtra("game_name", gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_INSTALL)
                .setContentTitle(gameName)
                .setContentText(getText(R.string.notification_download_and_install_game))
                .setSmallIcon(R.drawable.ic_refresh_white_24dp)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(INSTALL_NOTIFICATION_ID, notification)

        if (url != null) {
            try {
                game.saveStateToDB(IS_INSTALL)
                val zipfile = File(externalCacheDir, extractFilename(url))
                download(url, zipfile)
                zipfile.unzip(StorageHelper(this).getGamesDirectory())
                FileUtils.deleteQuietly(zipfile)
                game.saveStateToDB(INSTALLED)
                game.saveInstalledVersionToDB(game.version)
            } catch (e: IndexOutOfBoundsException) {
                // invalid url (exception from String.substring)
                sendNotification(getString(R.string.error), "Bad url: $url")
                game.saveStateToDB(NO_INSTALLED)
                return
            } catch (e: IOException) {
                sendNotification(getString(R.string.error), e.localizedMessage)
                game.saveStateToDB(NO_INSTALLED)
                return
            } catch (e: ZipException) {
                sendNotification(getString(R.string.error), e.localizedMessage)
                game.saveStateToDB(NO_INSTALLED)
                return
            }
        }

        stopForeground(true)
    }

    @Throws (IOException::class)
    private fun download(url: String, file: File) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Failed to download file: " + response)
        }
        FileOutputStream(file).use { toFile ->
            IOUtils.copy(response.body()?.byteStream(), toFile)
        }
    }

    private fun extractFilename(url: String): String {
        return url.substring(url.lastIndexOf('/') + 1)
    }

    private fun sendNotification(title: String, body: String){
        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_alert_white_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification.build())
    }
}
