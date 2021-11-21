/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.domain.repository.ResourceUpdater
import javax.inject.Inject

class ResourceUpdaterImpl @Inject constructor(
    private val storage: Storage,
) : ResourceUpdater {

    override suspend fun update() = withContext(Dispatchers.IO) {
        storage.getThemesDirectory().deleteRecursively()
        storage.copyAsset("themes", storage.getDataDirectory())

        storage.getSteadDirectory().deleteRecursively()
        storage.copyAsset("stead", storage.getDataDirectory())

        storage.getLangDirectory().deleteRecursively()
        storage.copyAsset("lang", storage.getDataDirectory())
    }
}