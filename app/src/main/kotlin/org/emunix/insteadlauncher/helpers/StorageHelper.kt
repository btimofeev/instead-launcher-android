package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.os.EnvironmentCompat
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class StorageHelper(val context: Context) {

    fun getAppFilesDirectory() : File {
        val storage : Array<File> = context.getExternalFilesDirs(null)
        for (file in storage) {
            if (file != null) {
                val state = EnvironmentCompat.getStorageState(file)
                if (Environment.MEDIA_MOUNTED == state) {
                    return file
                }
            }
        }
        // if external not presented use internal memory // todo check this
        return getDataDirectory()
    }

    fun getDataDirectory(): File = context.filesDir

    fun getLangDirectory(): File = File(getDataDirectory(), "lang")

    fun getSteadDirectory(): File = File(getDataDirectory(), "stead")

    fun getGamesDirectory(): File = File(getAppFilesDirectory(), "games")

    fun getSavesDirectory(): File = File(getAppFilesDirectory(), "saves")

    fun getThemesDirectory(): File = File(getAppFilesDirectory(), "themes")

    fun makeDirs() {
        val dirs = arrayOf(getGamesDirectory(), getSavesDirectory(), getThemesDirectory())
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
            val assets = assetManager.list(name)

            val dir = File(toPath, name)
            if (assets.isEmpty()) {
                FileUtils.copyInputStreamToFile(assetManager.open(name), dir)
            } else {
                FileUtils.forceMkdir(dir)
                for (i in 0 until assets.size) {
                    copyAsset(name + "/" + assets[i], toPath)
                }
            }
        } catch (e: IOException) {
            Log.e("copyAsset", e.localizedMessage)
        }
    }
}
