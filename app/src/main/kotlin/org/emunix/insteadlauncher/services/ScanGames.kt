/*
 * Copyright (c) 2019-2020 Boris Timofeev <btimofeev@emunix.org>
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
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.Game.State.INSTALLED
import org.emunix.insteadlauncher.data.Game.State.NO_INSTALLED
import org.emunix.insteadlauncher.helpers.GameParser
import org.emunix.insteadlauncher.storage.Storage
import org.emunix.insteadlauncher.helpers.saveInstalledVersionToDB
import org.emunix.insteadlauncher.helpers.saveStateToDB
import org.emunix.insteadlauncher.ui.installedgames.InstalledGamesActivity
import java.io.File
import java.util.*
import java.text.SimpleDateFormat
import javax.inject.Inject


class ScanGames : IntentService("ScanGames") {

    @Inject lateinit var storage: Storage
    @Inject lateinit var gameParser: GameParser

    override fun onHandleIntent(intent: Intent?) {
        val notification = createNotification()
        startForeground(InsteadLauncher.SCAN_GAMES_NOTIFICATION_ID, notification)

        InsteadLauncher.appComponent.inject(this)

        checkDeletedGames()
        checkNewGames()

        stopForeground(true)
    }

    private fun checkNewGames() {
        val installedGames = InsteadLauncher.db.games().getInstalledGames()
        val installedNames = arrayListOf<String>()
        installedGames.forEach {
            installedNames.add(it.name)
        }
        val gamesDir = storage.getGamesDirectory()
        val localNames = gamesDir.list() ?: return
        if (localNames.isEmpty())
            return

        val lang = Locale.getDefault().language
        val newNames = localNames.filterNot { installedNames.contains(it) }
        newNames.forEach {
            val gameDir = File(gamesDir, it)
            if (gameParser.isInsteadGame(gameDir)) {
                try {
                    val file = gameParser.getMainGameFile(gameDir)

                    val version = gameParser.getVersion(file, lang)
                    val title = gameParser.getTitle(file, lang)
                    val author = gameParser.getAuthor(file, lang)
                    val description = gameParser.getInfo(file, lang)
                    val size = FileUtils.sizeOfDirectory(gameDir)
                    val date = SimpleDateFormat("yyyy-MM-dd").format(Date())

                    val newGame = Game(it, title, author, date, version, size, "", "", "", description, "", "", version, INSTALLED)
                    InsteadLauncher.db.games().insert(newGame)

                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkDeletedGames() {
        val installedGames = InsteadLauncher.db.games().getInstalledGames()
        if (installedGames.isEmpty())
            return

        val installedNames = arrayListOf<String>()
        installedGames.forEach {
            installedNames.add(it.name)
        }
        val localNames = storage.getGamesDirectory().list() ?: return

        val deletedNames = installedNames.filterNot { localNames.contains(it) }

        deletedNames.forEach {
            val game = InsteadLauncher.db.games().getByName(it)
            if (game.url.isNotEmpty()) {
                game.saveStateToDB(NO_INSTALLED)
                game.saveInstalledVersionToDB("")
            } else { // delete local game
                InsteadLauncher.db.games().delete(game)
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, InstalledGamesActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, InsteadLauncher.CHANNEL_SCAN_GAMES)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_scan_games))
                .setSmallIcon(R.drawable.ic_search_24dp)
                .setContentIntent(pendingIntent)
                .build()
    }

    companion object {

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, ScanGames::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}