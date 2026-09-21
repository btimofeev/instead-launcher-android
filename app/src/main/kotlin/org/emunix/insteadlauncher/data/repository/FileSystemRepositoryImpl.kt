/*
 * Copyright (c) 2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.utils.unzip
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class FileSystemRepositoryImpl @Inject constructor(
    private val storage: Storage,
) : FileSystemRepository {

    override suspend fun createStorageDirectories() = withContext(Dispatchers.IO) {
        storage.createStorageDirectories()
    }

    override suspend fun deleteGameFromDisk(gameName: String) = withContext(Dispatchers.IO) {
        val gameDir = File(storage.getGamesDirectory(), gameName)
        gameDir.deleteRecursively()
        cleanupGameTempFiles(gameName)
        return@withContext
    }

    override suspend fun cleanupGameTempFiles(gameName: String) = withContext(Dispatchers.IO) {
        getTempZipFile(gameName).delete()
        getTempInstallDir(gameName).deleteRecursively()
        return@withContext
    }

    override suspend fun copyResourcesFromAssets() = withContext(Dispatchers.IO) {
        storage.getThemesDirectory().deleteRecursively()
        storage.copyAsset("themes", storage.getDataDirectory())

        storage.getSteadDirectory().deleteRecursively()
        storage.copyAsset("stead", storage.getDataDirectory())

        storage.getLangDirectory().deleteRecursively()
        storage.copyAsset("lang", storage.getDataDirectory())
    }

    override suspend fun getInstalledThemeNames(): List<String> = withContext(Dispatchers.IO) {
        val themes = mutableListOf<String>()
        val internalThemesDir = storage.getThemesDirectory()
        val externalThemesDir = storage.getUserThemesDirectory()
        themes.addAll(getInstalledThemeNamesFrom(internalThemesDir))
        if (internalThemesDir.canonicalFile != externalThemesDir.canonicalFile) {
            for (theme in getInstalledThemeNamesFrom(externalThemesDir)) {
                if (!themes.contains(theme))
                    themes.add(theme)
            }
        }
        return@withContext themes
    }

    override suspend fun installGame(gameName: String, zipStream: InputStream) = withContext(Dispatchers.IO) {
        val tmpDir = getTempInstallDir(gameName)
        val gameDir = File(storage.getGamesDirectory(), gameName)

        FileUtils.forceMkdir(gameDir.parentFile)
        tmpDir.deleteRecursively()
        FileUtils.forceMkdir(tmpDir)
        try {
            zipStream.unzip(tmpDir)
            val children = tmpDir.listFiles() ?: emptyArray()
            if (children.size == 1 && children[0].isDirectory) {
                gameDir.deleteRecursively()
                FileUtils.moveDirectory(children[0], gameDir)
            } else {
                gameDir.deleteRecursively()
                FileUtils.copyDirectory(tmpDir, gameDir)
            }
        } finally {
            cleanupGameTempFiles(gameName)
        }
    }

    private fun getTempZipFile(gameName: String) =
        File(storage.getAppFilesDirectory(), "$gameName.zip")

    private fun getTempInstallDir(gameName: String) =
        File(storage.getAppFilesDirectory(), ".tmp-$gameName")

    private fun getInstalledThemeNamesFrom(path: File): List<String> {
        val themes = mutableListOf<String>()
        if (path.exists()) {
            val dirs = path.listFiles()?.filter { it.isDirectory }
            dirs?.forEach { dir ->
                if (File(dir, "theme.ini").exists()) {
                    themes.add(dir.name)
                }
            }
        }
        return themes
    }
}