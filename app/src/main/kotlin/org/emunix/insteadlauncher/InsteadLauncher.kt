/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.annotation.AcraNotification
import org.acra.data.StringFormat
import org.emunix.insteadlauncher.data.GameDatabase
import org.emunix.insteadlauncher.di.AppComponent
import org.emunix.insteadlauncher.di.AppModule
import org.emunix.insteadlauncher.di.DaggerAppComponent
import org.emunix.insteadlauncher.helpers.ThemeHelper


@AcraCore(stopServicesOnCrash = true,
        reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraMailSender(mailTo = "btimofeev@emunix.org",
        reportFileName = "instead_launcher_crash_report.txt")
@AcraNotification(resText = R.string.error_crash_message,
        resTitle = R.string.error_crash_title,
        resSendButtonText = R.string.error_crash_send_button,
        resDiscardButtonText = R.string.error_crash_discard_button,
        resChannelName = R.string.channel_crash_report)
class InsteadLauncher: Application() {

    companion object {
        lateinit var appComponent: AppComponent
        private set

        lateinit var db: GameDatabase

        const val UPDATE_REPOSITORY_NOTIFICATION_ID: Int = 1000
        const val INSTALL_NOTIFICATION_ID: Int = 1001
        const val UNINSTALL_NOTIFICATION_ID: Int = 1002
        const val UPDATE_RESOURCES_NOTIFICATION_ID: Int = 1004
        const val SCAN_GAMES_NOTIFICATION_ID: Int = 1005

        const val CHANNEL_UPDATE_REPOSITORY = "org.emunix.insteadlauncher.channel.update_repo"
        const val CHANNEL_INSTALL = "org.emunix.insteadlauncher.channel.install_game"
        const val CHANNEL_UNINSTALL = "org.emunix.insteadlauncher.channel.delete_game"
        const val CHANNEL_UPDATE_RESOURCES = "org.emunix.insteadlauncher.channel.update_resources"
        const val CHANNEL_SCAN_GAMES = "org.emunix.insteadlauncher.channel.scan_games"

        const val DEFAULT_REPOSITORY = "http://instead-games.ru/xml.php"
        const val SANDBOX = "http://instead-games.ru/xml2.php"
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        createNotificationChannels()
        db =  appComponent.db()

        val storage = appComponent.storage()
        storage.createStorageDirectories()

        val sharedPreferences = appComponent.sharedPreferences()
        val themePref = sharedPreferences.getString("app_theme", ThemeHelper.DEFAULT_MODE)
        ThemeHelper.applyTheme(themePref!!)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG)
            ACRA.init(this)
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

            name = getString(R.string.channel_scan_games)
            channel = NotificationChannel(CHANNEL_SCAN_GAMES, name, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }
}