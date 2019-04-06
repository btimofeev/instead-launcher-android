/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UNINSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.UNINSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game.State.IS_DELETE
import org.emunix.insteadlauncher.data.Game.State.NO_INSTALLED
import org.emunix.insteadlauncher.helpers.StorageHelper
import org.emunix.insteadlauncher.helpers.saveInstalledVersionToDB
import org.emunix.insteadlauncher.helpers.saveStateToDB
import org.emunix.insteadlauncher.ui.game.GameActivity
import java.io.File
import java.io.IOException

class DeleteGame : IntentService("DeleteGame") {

    private lateinit var gameName: String

    override fun onHandleIntent(intent: Intent?) {
        gameName = intent?.getStringExtra("game_name") ?: return

        val notification = createNotification()
        startForeground(UNINSTALL_NOTIFICATION_ID, notification)

        val game = InsteadLauncher.db.games().getByName(gameName)
        try {
            game.saveStateToDB(IS_DELETE)
            val gameDir = File(StorageHelper(this).getGamesDirectory(), gameName)
            gameDir.deleteRecursively()
            game.saveStateToDB(NO_INSTALLED)
            game.saveInstalledVersionToDB("")
        } catch (e: IOException) {
            sendNotification(getString(R.string.error), e.localizedMessage)
            game.saveStateToDB(NO_INSTALLED)
            stopForeground(true)
            return
        }

        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, GameActivity::class.java)
        notificationIntent.putExtra("game_name", gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_UNINSTALL)
                .setContentTitle(gameName)
                .setContentText(getText(R.string.notification_delete_game))
                .setSmallIcon(R.drawable.ic_delete_white_24dp)
                .setContentIntent(pendingIntent)
                .build()
    }

    private fun sendNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, InsteadLauncher.CHANNEL_UNINSTALL)
                .setSmallIcon(R.drawable.ic_alert_white_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification.build())
    }


    companion object {

        @JvmStatic
        fun start(context: Context, game: String) {
            val intent = Intent(context, DeleteGame::class.java).apply {
                putExtra("game_name", game)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
