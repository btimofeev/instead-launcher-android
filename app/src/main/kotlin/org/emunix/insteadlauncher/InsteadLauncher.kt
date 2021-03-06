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
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.annotation.AcraNotification
import org.acra.data.StringFormat
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.helpers.ThemeHelper
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
@AcraCore(stopServicesOnCrash = true,
        reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraMailSender(mailTo = "btimofeev@emunix.org",
        reportFileName = "instead_launcher_crash_report.txt")
@AcraNotification(resText = R.string.error_crash_message,
        resTitle = R.string.error_crash_title,
        resSendButtonText = R.string.error_crash_send_button,
        resDiscardButtonText = R.string.error_crash_discard_button,
        resChannelName = R.string.channel_crash_report)
class InsteadLauncher: Application(), Configuration.Provider {

    companion object {

        lateinit var storage: Storage

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
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var preferencesProvider: PreferencesProvider

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        createNotificationChannels()

        GlobalScope.launch(Dispatchers.IO) {
            storage.createStorageDirectories()
        }

        ThemeHelper.applyTheme(preferencesProvider.appTheme)

        InsteadLauncher.storage = storage
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG)
            ACRA.init(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

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