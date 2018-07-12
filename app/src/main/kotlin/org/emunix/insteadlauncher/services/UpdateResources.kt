package org.emunix.insteadlauncher.services

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import androidx.core.app.NotificationCompat
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.StorageHelper
import org.emunix.insteadlauncher.ui.installedgames.InstalledGamesActivity

private const val REMOVE_THEMES_BEFORE_UPDATE = "org.emunix.insteadlauncher.extra.REMOVE_THEMES_BEFORE_UPDATE "

private const val PREFS_FILENAME = "version_prefs"
private const val PREF_RESOURCES_LAST_UPDATE = "resources_last_update"

class UpdateResources : IntentService("UpdateResources") {

    override fun onHandleIntent(intent: Intent?) {
        val removeBeforeUpdate = intent?.getBooleanExtra(REMOVE_THEMES_BEFORE_UPDATE, false) ?: false
        if (isNewAppVersion()) {
            val notification = createNotification()
            startForeground(InsteadLauncher.UPDATE_RESOURCES_NOTIFICATION_ID, notification)

            if (removeBeforeUpdate) {
                StorageHelper(this).getThemesDirectory().deleteRecursively()
            }
            StorageHelper(this).copyAsset("themes", StorageHelper(this).getAppFilesDirectory())

            StorageHelper(this).getSteadDirectory().deleteRecursively()
            StorageHelper(this).copyAsset("stead",  StorageHelper(this).getDataDirectory())

            StorageHelper(this).getLangDirectory().deleteRecursively()
            StorageHelper(this).copyAsset("lang", StorageHelper(this).getDataDirectory())

            saveCurrentAppVersion(InsteadLauncher().getVersionCode())

            stopForeground(true)
        }
    }

    private fun createNotification(): Notification? {
        val notificationIntent = Intent(this, InstalledGamesActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, InsteadLauncher.CHANNEL_UPDATE_RESOURCES)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_updating_resources))
                .setSmallIcon(R.drawable.ic_refresh_white_24dp)
                .setContentIntent(pendingIntent)
                .build()
    }

    private fun isNewAppVersion() : Boolean {
        val prefs = this.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong(PREF_RESOURCES_LAST_UPDATE, -1)
        if (lastUpdate != InsteadLauncher().getVersionCode()) {
            return true
        }
        return false
    }

    private fun saveCurrentAppVersion(value: Long) {
        val prefs = this.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(PREF_RESOURCES_LAST_UPDATE, value)
        editor.apply()
    }

    companion object {

        @JvmStatic
        fun startActionUpdate(context: Context, removeThemesBeforeUpdate: Boolean = false) {
            val intent = Intent(context, UpdateResources::class.java).apply {
                putExtra(REMOVE_THEMES_BEFORE_UPDATE, removeThemesBeforeUpdate)
            }
            context.startService(intent)
        }
    }
}
