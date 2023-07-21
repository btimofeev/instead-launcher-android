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
import androidx.navigation.NavDeepLinkBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.emunix.insteadlauncher.InsteadLauncher.Companion.CHANNEL_UPDATE_REPOSITORY
import org.emunix.insteadlauncher.InsteadLauncher.Companion.UPDATE_REPOSITORY_NOTIFICATION_ID
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.network.RepoUpdater
import org.emunix.insteadlauncher.presentation.launcher.LauncherActivity
import javax.inject.Inject

@AndroidEntryPoint
class UpdateRepository: IntentService("UpdateRepository") {

    @Inject lateinit var repoUpdater: RepoUpdater

    override fun onHandleIntent(intent: Intent?) {
        val notification = createNotification()
        startForeground(UPDATE_REPOSITORY_NOTIFICATION_ID, notification)

        runBlocking {
            repoUpdater.update()
        }

        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, LauncherActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = NavDeepLinkBuilder(this)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.repositoryFragment)
                .createPendingIntent()

        return NotificationCompat.Builder(this, CHANNEL_UPDATE_REPOSITORY)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_updating_repository))
                .setSmallIcon(R.drawable.ic_sync_24dp)
                .setContentIntent(pendingIntent)
                .build()
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
