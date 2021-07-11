/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.os.Build
import android.util.Log
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import javax.inject.Inject

class AppVersion @Inject constructor(
    private val context: Context,
    private val preferencesProvider: PreferencesProvider
) {

    @Suppress("DEPRECATION")
    fun getCode(): Long {
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

    fun getString(): String {
        var versionName = "N/A"
        try {
            val pinfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = pinfo.versionName
        } catch (e: Exception) {
            Log.e("INSTEAD Launcher", "App version is not available")
        }

        return versionName
    }

    fun isNewVersion(): Boolean {
        val lastUpdate = preferencesProvider.resourcesLastUpdate
        if (lastUpdate != getCode()) {
            return true
        }
        return false
    }

    fun saveCurrentVersion() {
        preferencesProvider.resourcesLastUpdate = getCode()
    }
}