/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.storage

import java.io.File

interface Storage {
    fun getAppFilesDirectory() : File
    fun getCacheDirectory() : File
    fun getDataDirectory(): File
    fun getLangDirectory(): File
    fun getSteadDirectory(): File
    fun getThemesDirectory(): File
    fun getGamesDirectory(): File
    fun getSavesDirectory(): File
    fun getUserThemesDirectory(): File
    fun createStorageDirectories()
    fun copyAsset(name: String, toPath: File)
}