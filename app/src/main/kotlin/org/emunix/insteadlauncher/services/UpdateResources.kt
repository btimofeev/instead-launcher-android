/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
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
import org.emunix.insteadlauncher.helpers.StorageHelper
import org.emunix.insteadlauncher.ui.installedgames.InstalledGamesActivity

class UpdateResources : IntentService("UpdateResources") {

    override fun onHandleIntent(intent: Intent?) {
        val notification = createNotification()
        startForeground(InsteadLauncher.UPDATE_RESOURCES_NOTIFICATION_ID, notification)

        if (AppVersion(this).isNewVersion()) {
            StorageHelper(this).getThemesDirectory().deleteRecursively()
            StorageHelper(this).copyAsset("themes", StorageHelper(this).getDataDirectory())

            StorageHelper(this).getSteadDirectory().deleteRecursively()
            StorageHelper(this).copyAsset("stead",  StorageHelper(this).getDataDirectory())

            StorageHelper(this).getLangDirectory().deleteRecursively()
            StorageHelper(this).copyAsset("lang", StorageHelper(this).getDataDirectory())

            AppVersion(this).saveCurrentVersion(AppVersion(this).getCode())
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
                .setSmallIcon(R.drawable.ic_refresh_black_24dp)
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
