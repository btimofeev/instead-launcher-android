/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.R
import java.io.File
import java.io.IOException

class StorageHelper(val context: Context) {

    fun getAppFilesDirectory() : File {
        val storage : Array<File?> = context.getExternalFilesDirs(null)
        for (file in storage) {
            if (file != null) {
                val state = EnvironmentCompat.getStorageState(file)
                if (state == Environment.MEDIA_MOUNTED) {
                    return file
                }
            }
        }
        // if external not presented use internal memory
        return getDataDirectory()
    }

    fun getCacheDirectory() : File {
        val storage : Array<File?> = context.externalCacheDirs
        for (file in storage) {
            if (file != null) {
                val state = EnvironmentCompat.getStorageState(file)
                if (state == Environment.MEDIA_MOUNTED) {
                    return file
                }
            }
        }
        // if external not presented use internal memory
        return context.cacheDir
    }

    fun getDataDirectory(): File = context.filesDir

    fun getLangDirectory(): File = File(getDataDirectory(), "lang")

    fun getSteadDirectory(): File = File(getDataDirectory(), "stead")

    fun getThemesDirectory(): File = File(getDataDirectory(), "themes")

    fun getGamesDirectory(): File = File(getAppFilesDirectory(), "games")

    fun getSavesDirectory(): File = File(getAppFilesDirectory(), "saves")

    fun getUserThemesDirectory(): File = File(getAppFilesDirectory(), "themes")

    fun makeDirs() {
        val dirs = arrayOf(getGamesDirectory(), getSavesDirectory(), getUserThemesDirectory())
        for (dir in dirs) {
            try {
                FileUtils.forceMkdir(dir)
            } catch (e: IOException) {
                context.showToast("Cannot create directory ${dir.absolutePath}")
            }
        }
    }

    fun copyAsset(name: String, toPath: File) {
        val assetManager = context.assets
        try {
            val assets = assetManager.list(name) ?: throw IOException()

            val dir = File(toPath, name)
            if (assets.isEmpty()) {
                FileUtils.copyInputStreamToFile(assetManager.open(name), dir)
            } else {
                FileUtils.forceMkdir(dir)
                for (element in assets) {
                    copyAsset("$name/$element", toPath)
                }
            }
        } catch (e: IOException) {
            NotificationHelper(context).showError(context.getString(R.string.error), context.getString(R.string.error_failed_to_copy_assets))
        }
    }
}
