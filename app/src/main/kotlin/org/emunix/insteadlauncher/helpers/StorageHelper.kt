package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class StorageHelper(val context: Context) {

    fun getAppSubDirectory(dir: String) : File {
        val storage : Array<File> = context.getExternalFilesDirs(dir)
        for (file in storage) {
            if (file != null) {
                val state = EnvironmentCompat.getStorageState(file)
                if (Environment.MEDIA_MOUNTED == state) {
                    return file
                }
            }
        }
        // if external not presented use internal memory // todo check this
        return context.filesDir
    }

    fun getGamesDirectory(): File = getAppSubDirectory("games")

    fun getSavesDirectory(): File = getAppSubDirectory("saves")

    fun getThemesDirectory(): File = getAppSubDirectory("themes")

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

}