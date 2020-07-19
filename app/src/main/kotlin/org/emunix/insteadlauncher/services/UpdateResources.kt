/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.AppVersion
import org.emunix.insteadlauncher.storage.Storage
import org.emunix.insteadlauncher.ui.installedgames.InstalledGamesActivity
import javax.inject.Inject

class UpdateResources : IntentService("UpdateResources") {

    @Inject lateinit var appVersion: AppVersion
    @Inject lateinit var storage: Storage

    override fun onHandleIntent(intent: Intent?) {
        val notification = createNotification()
        startForeground(InsteadLauncher.UPDATE_RESOURCES_NOTIFICATION_ID, notification)

        InsteadLauncher.appComponent.inject(this)

        if (appVersion.isNewVersion()) {
            storage.getThemesDirectory().deleteRecursively()
            storage.copyAsset("themes", storage.getDataDirectory())

            storage.getSteadDirectory().deleteRecursively()
            storage.copyAsset("stead",  storage.getDataDirectory())

            storage.getLangDirectory().deleteRecursively()
            storage.copyAsset("lang", storage.getDataDirectory())

            appVersion.saveCurrentVersion(appVersion.getCode())
        }

        stopForeground(true)
    }

    private fun createNotification(): Notification? {
        val notificationIntent = Intent(this, InstalledGamesActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, InsteadLauncher.CHANNEL_UPDATE_RESOURCES)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_updating_resources))
                .setSmallIcon(R.drawable.ic_sync_24dp)
                .setContentIntent(pendingIntent)
                .build()
    }

    companion object {

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, UpdateResources::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
