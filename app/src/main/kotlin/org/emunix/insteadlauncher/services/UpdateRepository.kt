package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UPDATE_REPOSITORY
import org.emunix.insteadlauncher.InsteadLauncher.Companion.DEFAULT_REPOSITORY
import org.emunix.insteadlauncher.InsteadLauncher.Companion.UPDATE_REPOSITORY_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.InsteadGamesXMLParser
import org.emunix.insteadlauncher.helpers.RxBus
import org.emunix.insteadlauncher.ui.repository.RepositoryActivity
import java.io.IOException

class UpdateRepository: IntentService("UpdateRepository") {

    override fun onHandleIntent(intent: Intent?) {
        val notificationIntent = Intent(this, RepositoryActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        RxBus.publish(UpdateRepoEvent(true))

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
            // TODO show better error
            RxBus.publish(UpdateRepoEvent(false, true, e.localizedMessage))
            return
        }

        val games: List<Game> = parseXML(xml)

        InsteadLauncher.db.games().updateRepository(games)

        RxBus.publish(UpdateRepoEvent(false))

        stopForeground(true)
    }

    @Throws (IOException::class)
    private fun fetchXML(): String {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(getRepo())
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        return response.body()!!.string()
    }

    private fun parseXML(xml: String): List<Game> = InsteadGamesXMLParser().parse(xml)

    private fun getRepo(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString("pref_repository", DEFAULT_REPOSITORY)!!
    }
}
