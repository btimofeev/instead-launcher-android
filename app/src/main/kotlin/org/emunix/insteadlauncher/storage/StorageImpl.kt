/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.storage

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.helpers.showToast
import java.io.File
import java.io.IOException

class StorageImpl (private val context: Context) : Storage {

    override fun getAppFilesDirectory() : File {
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

    override fun getCacheDirectory() : File {
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

    override fun getDataDirectory(): File = context.filesDir

    override fun getLangDirectory(): File = File(getDataDirectory(), "lang")

    override fun getSteadDirectory(): File = File(getDataDirectory(), "stead")

    override fun getThemesDirectory(): File = File(getDataDirectory(), "themes")

    override fun getGamesDirectory(): File = File(getAppFilesDirectory(), "games")

    override fun getSavesDirectory(): File = File(getAppFilesDirectory(), "saves")

    override fun getUserThemesDirectory(): File = File(getAppFilesDirectory(), "themes")

    override fun createStorageDirectories() {
        val dirs = arrayOf(getGamesDirectory(), getSavesDirectory(), getUserThemesDirectory())
        for (dir in dirs) {
            try {
                FileUtils.forceMkdir(dir)
            } catch (e: IOException) {
                context.showToast("Cannot create directory ${dir.absolutePath}")
            }
        }
    }

    override fun copyAsset(name: String, toPath: File) {
        val assetManager = context.assets
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
    }
}
