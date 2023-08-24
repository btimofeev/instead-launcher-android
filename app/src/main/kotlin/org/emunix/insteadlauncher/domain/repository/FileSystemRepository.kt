/*
 * Copyright (c) 2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.repository

import java.io.IOException
import java.io.InputStream

interface FileSystemRepository {

    @Throws(IOException::class)
    suspend fun createStorageDirectories()

    @Throws(IOException::class)
    suspend fun deleteGameFromDisk(gameName: String)

    @Throws(IOException::class)
    suspend fun copyResourcesFromAssets()

    suspend fun getInstalledThemeNames(): List<String>

    suspend fun installGame(gameName: String, zipStream: InputStream)
}