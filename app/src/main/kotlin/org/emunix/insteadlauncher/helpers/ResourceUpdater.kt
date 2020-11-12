/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.insteadlauncher.storage.Storage
import java.io.IOException
import javax.inject.Inject

class ResourceUpdater @Inject constructor(private val storage: Storage, private val appVersion: AppVersion) {

    suspend fun update(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (appVersion.isNewVersion()) {
                storage.getThemesDirectory().deleteRecursively()
                storage.copyAsset("themes", storage.getDataDirectory())

                storage.getSteadDirectory().deleteRecursively()
                storage.copyAsset("stead", storage.getDataDirectory())

                storage.getLangDirectory().deleteRecursively()
                storage.copyAsset("lang", storage.getDataDirectory())

                appVersion.saveCurrentVersion(appVersion.getCode())
            }
        } catch (e: IOException) {
            Log.d("ResourceUpdater", e.message, e)
            return@withContext false
        }
        return@withContext true
    }
}
