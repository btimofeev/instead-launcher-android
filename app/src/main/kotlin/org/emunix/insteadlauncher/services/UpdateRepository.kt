/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UPDATE_REPOSITORY
import org.emunix.insteadlauncher.InsteadLauncher.Companion.DEFAULT_REPOSITORY
import org.emunix.insteadlauncher.InsteadLauncher.Companion.SANDBOX
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
        val notification = createNotification()
        startForeground(UPDATE_REPOSITORY_NOTIFICATION_ID, notification)

        RxBus.publish(UpdateRepoEvent(true))

        val games: ArrayList<Game> = arrayListOf()

        try {
            val gamesMap: MutableMap<String, Game> = mutableMapOf()
            if (isSandboxEnabled()){
                gamesMap.putAll(parseXML(fetchXML(getSandbox())))
            }
            gamesMap.putAll(parseXML(fetchXML(getRepo())))
            gamesMap.forEach { (_, value) -> games.add(value) }
        } catch (e: IOException) {
            RxBus.publish(UpdateRepoEvent(false, false, true,
                    getString(R.string.error_server_return_unexpected_code, e.message)))
            stopForeground(true)
            return
        }

        InsteadLauncher.db.games().updateRepository(games)

        RxBus.publish(UpdateRepoEvent(isLoading = false, isGamesLoaded = true))

        ScanGames.start(this)

        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, RepositoryActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_UPDATE_REPOSITORY)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_updating_repository))
                .setSmallIcon(R.drawable.ic_refresh_black_24dp)
                .setContentIntent(pendingIntent)
                .build()
    }

    @Throws (IOException::class)
    private fun fetchXML(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("${response.code()}")
        return response.body()!!.string()
    }

    private fun parseXML(xml: String): Map<String, Game> = InsteadGamesXMLParser().parse(xml)

    private fun getRepo(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString("pref_repository", DEFAULT_REPOSITORY)!!
    }

    private fun getSandbox(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString("pref_sandbox", SANDBOX)!!
    }

    private fun isSandboxEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean("pref_sandbox_enabled", false)
    }

    companion object {

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, UpdateRepository::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
