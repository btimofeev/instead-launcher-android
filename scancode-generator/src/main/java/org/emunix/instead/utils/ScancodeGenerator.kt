/*
 * Copyright (C) 2015-2018 Anton Kolosov https://github.com/instead-hub/instead-android-ng
 * Copyright (c) 2019, 2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.utils

import android.view.KeyEvent
import java.util.Locale

/**
 * The class sends the scancode of the key to the handler
 *
 * Can only work with Cyrillic
 *
 * @property handler Handler that accepts scancode
 */
class ScancodeGenerator(
    private val handler: OnKeyCodeHandler
) {

    /**
     * Send key scancode to handler
     *
     * The function sends two events: first, a key press, then a release
     *
     * @param char Scancode symbol
     */
    fun sendScancode(char: Char) {
        val str = char.toString()
        when {
            str.matches("[а-я]".toRegex()) -> ru(char, false)
            str.matches("[А-Я]".toRegex()) -> ru(str.lowercase(Locale.getDefault()).first(), true)
        }
    }

    private fun ru(s: Char, shift: Boolean) {
        var k = -1

        when (s) {
            'й' -> k = KeyEvent.KEYCODE_Q
            'ц' -> k = KeyEvent.KEYCODE_W
            'у' -> k = KeyEvent.KEYCODE_E
            'к' -> k = KeyEvent.KEYCODE_R
            'е' -> k = KeyEvent.KEYCODE_T
            'н' -> k = KeyEvent.KEYCODE_Y
            'г' -> k = KeyEvent.KEYCODE_U
            'ш' -> k = KeyEvent.KEYCODE_I
            'щ' -> k = KeyEvent.KEYCODE_O
            'з' -> k = KeyEvent.KEYCODE_P
            'х' -> k = 71
            'ъ' -> k = 72

            'ф' -> k = KeyEvent.KEYCODE_A
            'ы' -> k = KeyEvent.KEYCODE_S
            'в' -> k = KeyEvent.KEYCODE_D
            'а' -> k = KeyEvent.KEYCODE_F
            'п' -> k = KeyEvent.KEYCODE_G
            'р' -> k = KeyEvent.KEYCODE_H
            'о' -> k = KeyEvent.KEYCODE_J
            'л' -> k = KeyEvent.KEYCODE_K
            'д' -> k = KeyEvent.KEYCODE_L
            'ж' -> k = 74
            'э' -> k = 75

            'я' -> k = KeyEvent.KEYCODE_Z
            'ч' -> k = KeyEvent.KEYCODE_X
            'с' -> k = KeyEvent.KEYCODE_C
            'м' -> k = KeyEvent.KEYCODE_V
            'и' -> k = KeyEvent.KEYCODE_B
            'т' -> k = KeyEvent.KEYCODE_N
            'ь' -> k = KeyEvent.KEYCODE_M
            'б' -> k = 55
            'ю' -> k = 56

            'ё' -> k = KeyEvent.KEYCODE_T
        }

        if (k > 0) {
            down(k, shift)
        }
    }

    private fun down(keyCode: Int, shift: Boolean) {
        keyPress(keyCode, shift)
        keyRelease(keyCode, shift)
    }

    private fun keyPress(keyCode: Int, shift: Boolean) {
        if (shift) {
            handler.onKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT)
        }
        handler.onKeyDown(keyCode)
    }

    private fun keyRelease(keyCode: Int, shift: Boolean) {
        handler.onKeyUp(keyCode)
        if (shift) {
            handler.onKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT)
        }
    }
}