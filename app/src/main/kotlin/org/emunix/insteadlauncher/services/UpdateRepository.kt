package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UPDATE_REPOSITORY
import org.emunix.insteadlauncher.InsteadLauncher.Companion.UPDATE_REPOSITORY_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.InsteadGamesXMLParser
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import java.io.IOException

class UpdateRepository: IntentService("UpdateRepository") {

    companion object {
        val REPO_URL: String = "http://instead-games.ru/xml.php"
    }

    override fun onHandleIntent(intent: Intent?) {
        val notificationIntent = Intent(this, RepositoryActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE_REPOSITORY)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_updating_repository))
                .setSmallIcon(R.drawable.ic_refresh_white_24dp)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(UPDATE_REPOSITORY_NOTIFICATION_ID, notification)

        val xml: String
        try {
            xml = fetchXML()
        } catch (e: IOException) {
            // TODO show error
            return
        }

        val games: List<Game> = parseXML(xml)

        InsteadLauncher.gamesDB.gameDao().insertAll(games)

        stopForeground(true)
    }

    @Throws (IOException::class)
    private fun fetchXML(): String {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(REPO_URL)
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unexpected code " + response)
        val xml: String = response.body()!!.string()
        return xml
    }

    private fun parseXML(xml: String): List<Game> {
        val xmlParser: InsteadGamesXMLParser = InsteadGamesXMLParser()
        val games: List<Game> = xmlParser.parse(xml)
        return games
    }
}