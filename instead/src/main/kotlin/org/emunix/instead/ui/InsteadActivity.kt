/*
 * Copyright (C) 2015-2018 Anton Kolosov https://github.com/instead-hub/instead-android-ng
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.ui

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.RelativeLayout
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.instead.R
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_BOTTOM_CENTER
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_BOTTOM_LEFT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_BOTTOM_RIGHT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_LEFT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_RIGHT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_TOP_CENTER
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_TOP_LEFT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.KEYBOARD_BUTTON_TOP_RIGHT
import org.emunix.instead.core_storage_api.data.Storage
import org.libsdl.app.SDLActivity
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class InsteadActivity: SDLActivity() {

    @Inject lateinit var preferenceProvider: PreferencesProvider
    @Inject lateinit var storage: Storage

    private var game : String? = ""
    private var playFromBeginning = false

    private lateinit var keyboardButton : ImageButton

    private var prefBackButton: String = ""

    override fun getLibraries(): Array<String> {
        return arrayOf(
                "SDL2",
                "SDL2_image",
                "SDL2_mixer",
                "SDL2_ttf",
                "lua",
                "charset",
                "iconv",
                "instead")
    }

    override fun getArguments(): Array<String> {
        val args : Array<String> = Array(14){""}
        args[0] = storage.getDataDirectory().absolutePath
        args[1] = storage.getAppFilesDirectory().absolutePath
        args[2] = storage.getGamesDirectory().absolutePath
        args[3] = storage.getUserThemesDirectory().absolutePath
        args[4] = Locale.getDefault().language
        args[5] = if (preferenceProvider.isMusicEnabled) "y" else "n"
        args[6] = if (preferenceProvider.isCursorEnabled) "y" else "n"
        args[7] = if (preferenceProvider.isOwnGameThemeEnabled) "y" else "n"
        args[8] = preferenceProvider.defaultInsteadTheme
        args[9] = if (preferenceProvider.isHiresEnabled) "y" else "n"
        args[10] = preferenceProvider.defaultInsteadTextSize
        args[11] = if (playFromBeginning) "y" else "n"
        args[12] = if (preferenceProvider.isGLHackEnabled) "y" else "n"
        args[13] = game ?: ""
        return args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The following line is to workaround AndroidRuntimeException: requestFeature() must be called before adding content
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        game = intent.extras?.getString("game_name")
        playFromBeginning = intent.extras?.getBoolean("play_from_beginning", false) ?: false

        prefBackButton = preferenceProvider.backButton
        initKeyboard()
    }

    private fun initKeyboard() {
        val prefKeyboardButton = preferenceProvider.keyboardButtonPosition
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

        if (prefKeyboardButton == PreferencesProvider.KEYBOARD_DO_NOT_SHOW_BUTTON) {
            keyboardButton.visibility = View.GONE
        }
    }

    private fun getKeyboardButtonGravity(s: String): Int {
        return when (s) {
            KEYBOARD_BUTTON_BOTTOM_LEFT   -> Gravity.BOTTOM or Gravity.LEFT
            KEYBOARD_BUTTON_BOTTOM_CENTER -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            KEYBOARD_BUTTON_BOTTOM_RIGHT  -> Gravity.BOTTOM or Gravity.RIGHT
            KEYBOARD_BUTTON_LEFT          -> Gravity.CENTER_VERTICAL or Gravity.LEFT
            KEYBOARD_BUTTON_RIGHT         -> Gravity.CENTER_VERTICAL or Gravity.RIGHT
            KEYBOARD_BUTTON_TOP_LEFT      -> Gravity.TOP or Gravity.LEFT
            KEYBOARD_BUTTON_TOP_CENTER    -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
            KEYBOARD_BUTTON_TOP_RIGHT     -> Gravity.TOP or Gravity.RIGHT
            else -> Gravity.BOTTOM or Gravity.LEFT

        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                if (prefBackButton == PreferencesProvider.BACK_BUTTON_OPEN_MENU) {
                    keyDispatcherState.startTracking(event, this)
                    return true
                }
            } else if (event.action == KeyEvent.ACTION_UP) {
                keyDispatcherState.handleUpEvent(event)
                if (event.isTracking && !event.isCanceled) {
                    if (prefBackButton == PreferencesProvider.BACK_BUTTON_OPEN_MENU) {
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

    override fun setOrientationBis(w: Int, h: Int, resizable: Boolean, hint: String) {
        val orientation = when {
            hint.contains("LandscapeRight") && hint.contains("LandscapeLeft") -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            hint.contains("LandscapeRight") -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hint.contains("LandscapeLeft") -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            hint.contains("Portrait") && hint.contains("PortraitUpsideDown") -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            hint.contains("Portrait") -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            hint.contains("PortraitUpsideDown") -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            else -> {
                if (!resizable)
                    if (w > h) {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    }
                else
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        Log.v("SDL", "setOrientation() orientation=$orientation width=$w height=$h resizable=$resizable hint=$hint")
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            mSingleton.requestedOrientation = orientation
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
    }

}