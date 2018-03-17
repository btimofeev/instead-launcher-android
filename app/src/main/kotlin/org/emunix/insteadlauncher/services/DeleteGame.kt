package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UNINSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.UNINSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.StorageHelper
import org.emunix.insteadlauncher.ui.game.GameActivity
import java.io.File
import java.io.IOException

class DeleteGame: IntentService("DeleteGame") {

    override fun onHandleIntent(intent: Intent?) {
        val gameName = intent?.getStringExtra("game_name") ?: return

        val notificationIntent = Intent(this, GameActivity::class.java)
        notificationIntent.putExtra("game_name", gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_UNINSTALL)
                .setContentTitle(gameName)
                .setContentText(getText(R.string.notification_delete_game))
                .setSmallIcon(R.drawable.ic_refresh_white_24dp)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(UNINSTALL_NOTIFICATION_ID, notification)

        try {
            val gameDir = File(StorageHelper(this).getGamesDirectory(), gameName)
            gameDir.deleteRecursively()
            updateDB(gameName)
        } catch (e: IOException) {
            sendNotification(getString(R.string.error), e.localizedMessage)
            return
        }

        stopForeground(true)
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

    private fun updateDB(name: String) {
        val game = InsteadLauncher.gamesDB.gameDao().getGameByName(name)
        game.installedVersion = ""
        game.installed = false
        InsteadLauncher.gamesDB.gameDao().insert(game)
    }
}
