/*
 * Copyright (c) 2018-2023 Boris Timofeev <btimofeev@emunix.org>
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
import org.acra.config.mailSender
import org.acra.config.notification
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.utils.ThemeSwitcherDelegate
import org.emunix.insteadlauncher.utils.writeToLog
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class InsteadLauncher : Application(), Configuration.Provider {

    companion object {

        const val INSTALL_NOTIFICATION_ID: Int = 1001
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
    lateinit var preferencesProvider: PreferencesProvider

    override fun onCreate() {
        super.onCreate()
        initLogger()
        setupNotificationChannels()
        ThemeSwitcherDelegate().applyTheme(preferencesProvider.appTheme)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG) {
            initCrashReporter()
        }
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    private fun initCrashReporter() {
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST
            stopServicesOnCrash = true

            mailSender {
                mailTo = "btimofeev@emunix.org"
                reportFileName = "instead_launcher_crash_report.txt"
            }

            notification {
                withTitle(getString(R.string.error_crash_title))
                withText(getString(R.string.error_crash_message))
                withSendButtonText(getString(R.string.error_crash_send_button))
                withDiscardButtonText(getString(R.string.error_crash_discard_button))
                withChannelName(getString(R.string.channel_crash_report))
            }
        }
    }

    @TargetApi(26)
    fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_LOW

            var name = getString(R.string.channel_install_game)
            var channel = NotificationChannel(CHANNEL_INSTALL, name, importance)
            notificationManager.createNotificationChannel(channel)

            name = getString(R.string.channel_scan_games)
            channel = NotificationChannel(CHANNEL_SCAN_GAMES, name, importance)
            notificationManager.createNotificationChannel(channel)

            unregisterLegacyNotificationChannels(notificationManager)
        }
    }

    @TargetApi(26)
    private fun unregisterLegacyNotificationChannels(notificationManager: NotificationManager) {
        try {
            with(notificationManager) {
                deleteNotificationChannel(CHANNEL_UPDATE_REPOSITORY)
                deleteNotificationChannel(CHANNEL_UPDATE_RESOURCES)
                deleteNotificationChannel(CHANNEL_UNINSTALL)
            }
        } catch (e: Throwable) {
            e.writeToLog()
        }
    }
}