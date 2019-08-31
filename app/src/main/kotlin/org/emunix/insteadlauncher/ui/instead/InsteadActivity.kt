/*
 * Copyright (C) 2015-2018 Anton Kolosov https://github.com/instead-hub/instead-android-ng
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.instead

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.preference.PreferenceManager
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.helpers.StorageHelper
import org.emunix.insteadlauncher.helpers.visible
import org.libsdl.app.SDLActivity
import java.util.*

class InsteadActivity: SDLActivity() {

    private var game : String? = ""
    private var playFromBeginning = false

    private lateinit var keyboardButton : ImageButton

    private var prefMusic: Boolean = true
    private var prefCursor: Boolean = false
    private var prefBuiltinTheme: Boolean = true
    private var prefDefaultTheme: String? = ""
    private var prefHires: Boolean = true
    private var prefTextSize: String? = ""
    private lateinit var prefKeyboardButton: String
    private var prefBackButton: String? = ""


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
        val args : Array<String> = Array(13){""}
        args[0] = StorageHelper(this).getDataDirectory().absolutePath
        args[1] = StorageHelper(this).getAppFilesDirectory().absolutePath
        args[2] = StorageHelper(this).getGamesDirectory().absolutePath
        args[3] = StorageHelper(this).getUserThemesDirectory().absolutePath
        args[4] = Locale.getDefault().language
        args[5] = if (prefMusic) "y" else "n"
        args[6] = if (prefCursor) "y" else "n"
        args[7] = if (prefBuiltinTheme) "y" else "n"
        args[8] = prefDefaultTheme ?: DEFAULT_THEME
        args[9] = if (prefHires) "y" else "n"
        args[10] = prefTextSize ?: DEFAULT_TEXT_SIZE
        args[11] = if (playFromBeginning) "y" else "n"
        args[12] = game ?: ""
        return args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The following line is to workaround AndroidRuntimeException: requestFeature() must be called before adding content
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        game = intent.extras?.getString("game_name")
        playFromBeginning = intent.extras?.getBoolean("play_from_beginning", false) ?: false

        getPreferences()
        initKeyboard()
    }

    private fun getPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefMusic = prefs.getBoolean("pref_music", true)
        prefCursor = prefs.getBoolean("pref_cursor", false)
        prefBuiltinTheme = prefs.getBoolean("pref_enable_game_theme", true)
        prefDefaultTheme = prefs.getString("pref_default_theme", DEFAULT_THEME)
        prefHires = prefs.getBoolean("pref_hires", true)
        prefTextSize = prefs.getString("pref_text_size", DEFAULT_TEXT_SIZE)
        prefKeyboardButton = prefs?.getString("pref_keyboard_button", "bottom_left") ?: "bottom_left"
        prefBackButton = prefs.getString("pref_back_button", "exit_game")
    }


    private fun initKeyboard() {
        val keyboardLayout = RelativeLayout(this)
        val rlp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        keyboardLayout.gravity = getKeyboardButtonGravity(prefKeyboardButton)

        keyboardButton = ImageButton(this)
        keyboardButton.background = null
        keyboardButton.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        keyboardButton.setImageResource(R.drawable.ic_keyboard_outline_bluegrey_24dp)
        keyboardButton.setOnClickListener {
            // send the event to INSTEAD so that it shows the keyboard
            onNativeKeyDown(KeyEvent.KEYCODE_F12)
        }

        keyboardLayout.addView(keyboardButton)
        addContentView(keyboardLayout, rlp)

        if (prefKeyboardButton == "do_not_show_button") {
            keyboardButton.visible(false)
        }
    }

    private fun getKeyboardButtonGravity(s: String): Int {
        return when (s) {
            "bottom_left"   -> Gravity.BOTTOM or Gravity.LEFT
            "bottom_center" -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            "bottom_right"  -> Gravity.BOTTOM or Gravity.RIGHT
            "left"          -> Gravity.CENTER_VERTICAL or Gravity.LEFT
            "right"         -> Gravity.CENTER_VERTICAL or Gravity.RIGHT
            "top_left"      -> Gravity.TOP or Gravity.LEFT
            "top_center"    -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
            "top_right"     -> Gravity.TOP or Gravity.RIGHT
            else -> Gravity.BOTTOM or Gravity.LEFT

        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                if (prefBackButton == "open_menu") {
                    keyDispatcherState.startTracking(event, this)
                    return true
                }
            } else if (event.action == KeyEvent.ACTION_UP) {
                keyDispatcherState.handleUpEvent(event)
                if (event.isTracking && !event.isCanceled) {
                    if (prefBackButton == "open_menu") {
                        toggleMenu()
                        return true
                    }
                }
            }
            return super.dispatchKeyEvent(event)
        } else {
            return super.dispatchKeyEvent(event)
        }
    }

    private external fun toggleMenu()

    companion object {
        // This method is called by native instead_launcher.c using JNI.
        @JvmStatic
        fun unlockRotation() {
            mSingleton.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        @JvmStatic
        private fun getScreenSize(): String {
            val display = mSingleton.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return "${size.x}x${size.y}"
        }

        const val DEFAULT_THEME = "mobile"
        const val DEFAULT_TEXT_SIZE = "130"
    }

}