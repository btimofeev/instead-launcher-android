package org.emunix.insteadlauncher

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import android.content.Context
import android.os.Build
import org.emunix.insteadlauncher.data.GameDatabase
import org.emunix.insteadlauncher.helpers.StorageHelper

class InsteadLauncher: Application() {

    companion object {
        lateinit var db: GameDatabase

        const val UPDATE_REPOSITORY_NOTIFICATION_ID: Int = 1000
        const val INSTALL_NOTIFICATION_ID: Int = 1001
        const val UNINSTALL_NOTIFICATION_ID: Int = 1002
        const val UPDATE_RESOURCES_NOTIFICATION_ID: Int = 1004

        const val CHANNEL_UPDATE_REPOSITORY = "org.emunix.insteadlauncher.channel.update_repo"
        const val CHANNEL_INSTALL = "org.emunix.insteadlauncher.channel.install_game"
        const val CHANNEL_UNINSTALL = "org.emunix.insteadlauncher.channel.delete_game"
        const val CHANNEL_UPDATE_RESOURCES = "org.emunix.insteadlauncher.channel.update_resources"

        const val DEFAULT_REPOSITORY = "http://instead-games.ru/xml.php"
        const val SANDBOX = "http://instead-games.ru/xml2.php"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        db =  Room.databaseBuilder(this, GameDatabase::class.java, "games.db").build()
        StorageHelper(this).makeDirs()
    }

    @TargetApi(26)
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_LOW

            var name = getString(R.string.channel_install_game)
            var channel = NotificationChannel(CHANNEL_INSTALL, name, importance)
            notificationManager.createNotificationChannel(channel)

            name = getString(R.string.channel_delete_game)
            channel = NotificationChannel(CHANNEL_UNINSTALL, name, importance)
            notificationManager.createNotificationChannel(channel)

            name = getString(R.string.channel_update_repo)
            channel = NotificationChannel(CHANNEL_UPDATE_REPOSITORY, name, importance)
            notificationManager.createNotificationChannel(channel)

            name = getString(R.string.channel_update_resources)
            channel = NotificationChannel(CHANNEL_UPDATE_RESOURCES, name, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getVersionCode(context: Context): Long {
        var version: Long = 0
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            version = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                info.versionCode.toLong()
            } else {
                info.longVersionCode
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return version
    }
}