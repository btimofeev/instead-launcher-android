/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R

class NotificationHelper(val context: Context) {

    fun showError(title: String, body: String, intent: PendingIntent? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, InsteadLauncher.CHANNEL_INSTALL)
                .setSmallIcon(R.drawable.ic_alert_white_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))

        if (intent != null)
            notification.setContentIntent(intent)

        notificationManager.notify(2, notification.build())
    }
}