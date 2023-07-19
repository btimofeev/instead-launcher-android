/*
 * Copyright (c) 2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import javax.inject.Inject

class FileSystemRepositoryImpl @Inject constructor(
    private val storage: Storage,
) : FileSystemRepository {

    override suspend fun createStorageDirectories() = withContext(Dispatchers.IO) {
        storage.createStorageDirectories()
    }

    override suspend fun copyResourcesFromAssets() = withContext(Dispatchers.IO) {
        storage.getThemesDirectory().deleteRecursively()
        storage.copyAsset("themes", storage.getDataDirectory())

        storage.getSteadDirectory().deleteRecursively()
        storage.copyAsset("stead", storage.getDataDirectory())

        storage.getLangDirectory().deleteRecursively()
        storage.copyAsset("lang", storage.getDataDirectory())
    }
}