/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UNINSTALL
import org.emunix.insteadlauncher.InsteadLauncher.Companion.UNINSTALL_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game.State.IS_DELETE
import org.emunix.insteadlauncher.data.Game.State.NO_INSTALLED
import org.emunix.insteadlauncher.helpers.saveInstalledVersionToDB
import org.emunix.insteadlauncher.helpers.saveStateToDB
import org.emunix.insteadlauncher.helpers.NotificationHelper
import org.emunix.insteadlauncher.ui.launcher.LauncherActivity
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
            val gameDir = File(InsteadLauncher.appComponent.storage().getGamesDirectory(), gameName)
            gameDir.deleteRecursively()

            if (game.url.isNotEmpty()) {
                game.saveStateToDB(NO_INSTALLED)
                game.saveInstalledVersionToDB("")
            } else { // delete local game
                InsteadLauncher.db.games().delete(game)
            }

        } catch (e: IOException) {
            NotificationHelper(this).showError(getString(R.string.error), getString(R.string.error_failed_to_delete_file))
            game.saveStateToDB(NO_INSTALLED)
            stopForeground(true)
            return
        }

        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, LauncherActivity::class.java)
        notificationIntent.putExtra("game_name", gameName)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = NavDeepLinkBuilder(this)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.gameFragment)
                .setArguments(bundleOf("game_name" to gameName))
                .createPendingIntent()

        return NotificationCompat.Builder(this, CHANNEL_UNINSTALL)
                .setContentTitle(gameName)
                .setContentText(getText(R.string.notification_delete_game))
                .setSmallIcon(R.drawable.ic_delete_white_24dp)
                .setContentIntent(pendingIntent)
                .build()
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
