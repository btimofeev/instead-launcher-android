/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import android.content.Context
import android.os.Build
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import timber.log.Timber
import javax.inject.Inject

class AppVersionRepositoryImpl @Inject constructor(
    private val context: Context,
    private val preferencesProvider: PreferencesProvider
) : AppVersionRepository {

    @Suppress("DEPRECATION")
    override val versionCode: Long
        get() {
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
                Timber.tag("INSTEAD Launcher").e(e)
            }
            return version
        }

    override val versionName: String
        get() {
            var versionName = "N/A"
            try {
                val pinfo = context.packageManager.getPackageInfo(context.packageName, 0)
                versionName = pinfo.versionName
            } catch (e: Exception) {
                Timber.tag("INSTEAD Launcher").e("App version is not available")
            }

            return versionName
        }

    override fun isNewVersion(): Boolean {
        val lastUpdate = preferencesProvider.resourcesLastUpdate
        if (lastUpdate != versionCode) {
            return true
        }
        return false
    }

    override fun saveCurrentVersion() {
        preferencesProvider.resourcesLastUpdate = versionCode
    }
}