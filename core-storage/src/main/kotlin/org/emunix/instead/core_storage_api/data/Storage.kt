/*
 * Copyright (c) 2020-2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.core_storage_api.data

import java.io.File
import java.io.IOException

interface Storage {

    fun getAppFilesDirectory(): File

    fun getCacheDirectory(): File

    fun getDataDirectory(): File

    fun getLangDirectory(): File

    fun getSteadDirectory(): File

    fun getThemesDirectory(): File

    fun getGamesDirectory(): File

    fun getSavesDirectory(): File

    fun getUserThemesDirectory(): File

    @Throws(IOException::class)
    fun createStorageDirectories()

    fun copyAsset(name: String, toPath: File)
}