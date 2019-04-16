/*
 * Copyright (C) 2015-2018 Anton Kolosov https://github.com/instead-hub/instead-android-ng
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.instead

import android.view.KeyEvent
import org.libsdl.app.SDLActivity

object GenerateScancode {
    
    @JvmStatic
    fun forUnichar(c: Char) {
        val str = Character.toString(c)
        when {
            str.matches("[а-я]".toRegex()) -> ru(c, false)
            str.matches("[А-Я]".toRegex()) -> ru(str.toLowerCase()[0], true)
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
            SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT)
        }
        SDLActivity.onNativeKeyDown(keyCode)
    }

    private fun keyRelease(keyCode: Int, shift: Boolean) {
        SDLActivity.onNativeKeyUp(keyCode)
        if (shift) {
            SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT)
        }
    }
}