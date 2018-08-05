package org.emunix.insteadlauncher.ui.instead

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import org.emunix.insteadlauncher.helpers.StorageHelper
import org.libsdl.app.SDLActivity
import java.util.*


class InsteadActivity: SDLActivity() {

    private lateinit var game : String

    private lateinit var modes: List<Point>

    private lateinit var inputLayout: InputLayout

    override fun getLibraries(): Array<String> {
        return arrayOf("SDL2",
                "SDL2_image",
                "SDL2_mixer",
                "SDL2_ttf",
                "lua",
                "charset",
                "iconv",
                "instead")
    }

    override fun getArguments(): Array<String> {
        val args : Array<String> = Array(7){""}
        args[0] = StorageHelper(this).getDataDirectory().absolutePath
        args[1] = StorageHelper(this).getAppFilesDirectory().absolutePath
        args[2] = StorageHelper(this).getGamesDirectory().absolutePath
        args[3] = StorageHelper(this).getThemesDirectory().absolutePath
        args[4] = getModesString()
        args[5] = Locale.getDefault().language
        args[6] = game
        return args
    }

    private fun getModesString(): String {
        val strModesList = modes.map { it -> "${it.x}x${it.y}" }
        return strModesList.joinToString(separator = ",")
    }

    private fun enableHWA() {
        window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
    }

    private fun getModes(): List<Point> {
        val result = ArrayList<Point>()
        val portraitSize = Point()
        val landscapeSize = Point()
        val display = windowManager.defaultDisplay
        setOrientation(0, 1, false, "Portrait")
        display.getSize(portraitSize)
        result.add(portraitSize)
        setOrientation(1, 0, false, "LandscapeRight")
        display.getSize(landscapeSize)
        result.add(landscapeSize)
        setOrientation(0, 0, true, "")
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The following line is to workaround AndroidRuntimeException: requestFeature() must be called before adding content
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        enableHWA()
        super.onCreate(savedInstanceState)

        game = intent.extras.getString("game_name")

        modes = getModes()

        inputLayout = InputLayout(this)
        addContentView(inputLayout, InputLayout.params)

    }

    companion object {
        // This method is called by native instead_launcher.c using JNI.
        @JvmStatic
        fun unlockRotation() {
            mSingleton.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

}