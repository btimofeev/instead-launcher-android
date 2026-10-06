/*
 * Copyright (C) 2015-2018 Anton Kolosov https://github.com/instead-hub/instead-android-ng
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.ui

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.Window
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
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
import org.emunix.instead_api.GameDefaultsApi
import org.libsdl.app.SDLActivity
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class InsteadActivity: SDLActivity() {

    @Inject lateinit var preferenceProvider: PreferencesProvider
    @Inject lateinit var storage: Storage
    @Inject lateinit var gameDefaultsApi: GameDefaultsApi

    private var game : String? = ""
    private var playFromBeginning = false

    private var isTelevision = false

    private val tvOkKeyCodes = intArrayOf(
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
    )

    private val tvDpadKeyCodes = mapOf(
            KeyEvent.KEYCODE_DPAD_UP to intArrayOf(0, -1),
            KeyEvent.KEYCODE_DPAD_DOWN to intArrayOf(0, 1),
            KeyEvent.KEYCODE_DPAD_LEFT to intArrayOf(-1, 0),
            KeyEvent.KEYCODE_DPAD_RIGHT to intArrayOf(1, 0),
    )

    private var mouseMode = false
    private var okTapClick: Runnable? = null
    private var okTapDouble = false

    private val uiHandler = Handler(Looper.getMainLooper())

    private lateinit var keyboardButton : ImageButton

    private var prefBackButton: String = ""

    override fun getLibraries(): Array<String> {
        return arrayOf(
                "SDL3",
                "SDL3_image",
                "SDL3_mixer",
                "SDL3_ttf",
                "luajit",
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
        args[6] = if (preferenceProvider.isCursorEnabled || gameDefaultsApi.isTelevision()) "y" else "n"
        args[7] = if (preferenceProvider.isOwnGameThemeEnabled) "y" else "n"
        args[8] = gameDefaultsApi.resolveTheme()
        args[9] = if (preferenceProvider.isHiresEnabled) "y" else "n"
        args[10] = gameDefaultsApi.resolveTextScale()
        args[11] = if (playFromBeginning) "y" else "n"
        args[12] = game ?: ""
        args[13] = gameDefaultsApi.resolveKeyboardMode()
        return args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The following line is to workaround AndroidRuntimeException: requestFeature() must be called before adding content
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        game = intent.extras?.getString("game_name")
        playFromBeginning = intent.extras?.getBoolean("play_from_beginning", false) ?: false

        isTelevision = gameDefaultsApi.isTelevision()
        prefBackButton = preferenceProvider.backButton
        initKeyboard()
    }

    override fun onDestroy() {
        resetOkTap()
        super.onDestroy()
    }

    private fun initKeyboard() {
        val prefKeyboardButton = gameDefaultsApi.resolveKeyboardButtonPosition()
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
            if (isTelevision) dispatchTvKeyEvent(event) else dispatchPhoneKeyEvent(event)

    private fun dispatchPhoneKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_BACK) {
            return super.dispatchKeyEvent(event)
        }
        handleBackKey(event) { performBackAction() }
        return true
    }

    private fun dispatchTvKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> handleBackKey(event) {
                if (isLongPress(event)) performBackAction() else toggleFrame()
            }
            in tvOkKeyCodes -> handleTvOkKey(event)
            in tvDpadKeyCodes -> if (!handleMouseModeDpad(event)) {
                return super.dispatchKeyEvent(event)
            }
            else -> return super.dispatchKeyEvent(event)
        }
        return true
    }

    private fun handleBackKey(event: KeyEvent, onBackPressed: () -> Unit) {
        when {
            event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 ->
                keyDispatcherState.startTracking(event, this)
            event.action == KeyEvent.ACTION_UP -> {
                keyDispatcherState.handleUpEvent(event)
                if (event.isTracking && !event.isCanceled) {
                    onBackPressed()
                }
            }
        }
    }

    private fun handleTvOkKey(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    keyDispatcherState.startTracking(event, this)
                    cancelOkTapClick()
                }
            }
            KeyEvent.ACTION_UP -> {
                keyDispatcherState.handleUpEvent(event)
                if (event.isTracking && !event.isCanceled) {
                    when {
                        isLongPress(event) -> {
                            resetOkTap()
                            toggleMenu()
                        }
                        okTapDouble -> {
                            resetOkTap()
                            toggleInputMode()
                        }
                        else -> scheduleOkClick(event.keyCode)
                    }
                } else {
                    resetOkTap()
                }
            }
        }
    }

    private fun handleMouseModeDpad(event: KeyEvent): Boolean {
        if (!mouseMode || isTextInputActive()) return false
        if (event.action == KeyEvent.ACTION_DOWN) {
            val direction = tvDpadKeyCodes.getValue(event.keyCode)
            val speed = (event.repeatCount / SPEED_REPEAT_STEP + 1).coerceAtMost(MAX_CURSOR_SPEED)
            moveCursor(direction[0], direction[1], speed)
        }
        return true
    }

    private fun cancelOkTapClick() {
        val click = okTapClick ?: return
        uiHandler.removeCallbacks(click)
        okTapClick = null
        okTapDouble = true
    }

    private fun resetOkTap() {
        okTapClick?.let { uiHandler.removeCallbacks(it) }
        okTapClick = null
        okTapDouble = false
    }

    private fun scheduleOkClick(keyCode: Int) {
        val click = Runnable {
            okTapClick = null
            sendOkClick(keyCode)
        }
        okTapClick = click
        uiHandler.postDelayed(click, ViewConfiguration.getDoubleTapTimeout().toLong())
    }

    private fun sendOkClick(keyCode: Int) {
        if (mouseMode) {
            clickCursor(true)
            clickCursor(false)
        } else {
            onNativeKeyDown(keyCode)
            onNativeKeyUp(keyCode)
        }
    }

    private fun toggleInputMode() {
        mouseMode = !mouseMode
        Toast.makeText(
                this,
                if (mouseMode) R.string.input_mode_mouse else R.string.input_mode_links,
                Toast.LENGTH_SHORT,
        ).show()
    }

    private fun isLongPress(event: KeyEvent): Boolean =
            event.eventTime - event.downTime >= ViewConfiguration.getLongPressTimeout()

    private fun performBackAction() {
        if (prefBackButton == PreferencesProvider.BACK_BUTTON_OPEN_MENU) {
            toggleMenu()
        } else {
            // "exit game": SDL3 consumes the back key and forwards it to the
            // game as SDLK_AC_BACK, so finish the activity ourselves.
            finish()
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

    private fun toggleMenu() {
        onNativeKeyDown(KeyEvent.KEYCODE_ESCAPE)
        onNativeKeyUp(KeyEvent.KEYCODE_ESCAPE)
    }

    private fun toggleFrame() {
        onNativeKeyDown(KeyEvent.KEYCODE_TAB)
        onNativeKeyUp(KeyEvent.KEYCODE_TAB)
    }

    private external fun moveCursor(dx: Int, dy: Int, speed: Int)

    private external fun clickCursor(down: Boolean)

    private external fun isTextInputActive(): Boolean

    companion object {
        private const val SPEED_REPEAT_STEP = 4
        private const val MAX_CURSOR_SPEED = 5

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