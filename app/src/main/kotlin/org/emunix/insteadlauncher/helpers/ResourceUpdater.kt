/*
 * Copyright (c) 2020-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.instead.core_storage_api.data.Storage
import timber.log.Timber
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

                appVersion.saveCurrentVersion()
            }
        } catch (e: IOException) {
            Timber.d(e)
            return@withContext false
        }
        return@withContext true
    }
}
