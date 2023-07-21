/*
 * Copyright (c) 2019-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.usecase.ScanAndUpdateLocalGamesUseCase
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import javax.inject.Inject

@AndroidEntryPoint
class ScanGames : IntentService("ScanGames") {

    @Inject
    lateinit var scanAndUpdateLocalGamesUseCase: ScanAndUpdateLocalGamesUseCase

    override fun onHandleIntent(intent: Intent?) {
        val notification = createNotification()
        startForeground(InsteadLauncher.SCAN_GAMES_NOTIFICATION_ID, notification)

        runBlocking {
            scanAndUpdateLocalGamesUseCase()
        }

        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, LauncherActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val flags = if (VERSION.SDK_INT >= VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            flags
        )

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
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}