package org.emunix.insteadlauncher.ui.instead

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import org.emunix.insteadlauncher.R
import org.libsdl.app.SDLActivity




class InputLayout(context: Context) : RelativeLayout(context) {
    private val kbdButton: ImageButton
    private val view: View
    private val activity: Activity = context as Activity

    init {
        view = activity.layoutInflater.inflate(R.layout.input_layout, null, false)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        layoutParams = params
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        addView(view)
        kbdButton = findViewById(R.id.kbdButton)
        //kbdButton.setBackgroundResource(R.drawable.ic_keyboard_outline_24dp)
        kbdButton.setOnClickListener { open() }
    }

    fun open() {
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        im.showSoftInput(view, InputMethodManager.SHOW_FORCED)
        activity.onWindowFocusChanged(true)  // keyboard input not working bug
    }

    fun close() {
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        when (action) {
            KeyEvent.ACTION_DOWN, KeyEvent.ACTION_MULTIPLE -> {
                if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                    if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                        shift = true
                    } else {
                        // Keys.keyPress(keyCode, false); -- do nothing, will be handled on keyup
                    }
                } else {
                    val characters = event.characters
                    if (characters != null && characters.isNotEmpty()) {
                        Keys.inputText(characters, IN_MAX)
                    }
                }
                return true
            }
            KeyEvent.ACTION_UP -> if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                    shift = false
                } else {
                    // Simulate press-release cycle on keyup
                    Keys.down(keyCode, shift)
                }
                return true
            }
        }
        return false
    }

    companion object {

        private var shift = false
        const val IN_MAX = 16

        val params: ViewGroup.LayoutParams
            get() = ViewGroup.LayoutParams(-1, -1)
    }

    object Keys {

        private fun keyPress(keyCode: Int, shift: Boolean) {
            if (shift) {
                SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT)
            }
            waitFun()
            SDLActivity.onNativeKeyDown(keyCode)
        }

        private fun keyRelease(keyCode: Int, shift: Boolean) {
            SDLActivity.onNativeKeyUp(keyCode)
            waitFun()
            if (shift) {
                SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT)
            }
        }

        fun down(keyCode: Int, shift: Boolean) {
            keyPress(keyCode, shift)
            waitFun()
            keyRelease(keyCode, shift)
        }

        private fun waitFun() {
            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                // no op
            }

        }

        fun key(c: Char) {
            val str = Character.toString(c)
            when {
                str.matches("[0-9]".toRegex()) -> Num(c)
                str.matches("[a-z]".toRegex()) -> Eng(c, false)
                str.matches("[A-Z]".toRegex()) -> Eng(str.toLowerCase()[0], true)
                str.matches("[а-я]".toRegex()) -> Rus(c, false)
                str.matches("[А-Я]".toRegex()) -> Rus(str.toLowerCase()[0], true)
                else -> Other(c)
            }
        }

        fun inputText(s: String, maxlen: Int) {
            var s = s
            var len = s.length
            if (len > maxlen) {
                s = s.substring(0, maxlen)
                len = s.length
            }

            for (i in 0 until len) {
                val c = s[i]
                Keys.key(c)
            }
        }

        private fun Eng(s: Char, shift: Boolean) {
            var k = -1

            when (s) {
                'q' -> k = KeyEvent.KEYCODE_Q
                'w' -> k = KeyEvent.KEYCODE_W
                'e' -> k = KeyEvent.KEYCODE_E
                'r' -> k = KeyEvent.KEYCODE_R
                't' -> k = KeyEvent.KEYCODE_T
                'y' -> k = KeyEvent.KEYCODE_Y
                'u' -> k = KeyEvent.KEYCODE_U
                'i' -> k = KeyEvent.KEYCODE_I
                'o' -> k = KeyEvent.KEYCODE_O
                'p' -> k = KeyEvent.KEYCODE_P

                'a' -> k = KeyEvent.KEYCODE_A
                's' -> k = KeyEvent.KEYCODE_S
                'd' -> k = KeyEvent.KEYCODE_D
                'f' -> k = KeyEvent.KEYCODE_F
                'g' -> k = KeyEvent.KEYCODE_G
                'h' -> k = KeyEvent.KEYCODE_H
                'j' -> k = KeyEvent.KEYCODE_J
                'k' -> k = KeyEvent.KEYCODE_K
                'l' -> k = KeyEvent.KEYCODE_L

                'z' -> k = KeyEvent.KEYCODE_Z
                'x' -> k = KeyEvent.KEYCODE_X
                'c' -> k = KeyEvent.KEYCODE_C
                'v' -> k = KeyEvent.KEYCODE_V
                'b' -> k = KeyEvent.KEYCODE_B
                'n' -> k = KeyEvent.KEYCODE_N
                'm' -> k = KeyEvent.KEYCODE_M
            }

            if (k > 0) {
                down(k, shift)
            }
        }

        private fun Rus(s: Char, shift: Boolean) {
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

        private fun Num(s: Char) {
            var k = -1
            when (s) {
                '0' -> k = KeyEvent.KEYCODE_0
                '1' -> k = KeyEvent.KEYCODE_1
                '2' -> k = KeyEvent.KEYCODE_2
                '3' -> k = KeyEvent.KEYCODE_3
                '4' -> k = KeyEvent.KEYCODE_4
                '5' -> k = KeyEvent.KEYCODE_5
                '6' -> k = KeyEvent.KEYCODE_6
                '7' -> k = KeyEvent.KEYCODE_7
                '8' -> k = KeyEvent.KEYCODE_8
                '9' -> k = KeyEvent.KEYCODE_9
            }

            if (k > 0) {
                down(k, false)
            }
        }

        private fun Other(s: Char) {
            var k = -1
            var shift = false
            when (s) {
                ' ' -> k = KeyEvent.KEYCODE_SPACE
                '-' -> k = 69
                '.' -> k = 56
                ',' -> k = 55
                '!' -> {
                    k = 8
                    shift = true
                }
                '?' -> {
                    k = 76
                    shift = true
                }
                ';' -> {
                    k = 74
                    shift = true
                }
                ':' -> {
                    k = 56
                    shift = true
                }
                '*' -> k = 17
                '/' -> k = 76
                '(' -> {
                    k = 16
                    shift = true
                }
                ')' -> {
                    k = 7
                    shift = true
                }
                '@' -> k = 77
                '$' -> {
                    k = 11
                    shift = true
                }
                '&' -> {
                    k = 14
                    shift = true
                }
                '%' -> {
                    k = 12
                    shift = true
                }
                '"' -> {
                    k = 75
                    shift = true
                }
            }

            if (k > 0) {
                down(k, shift)
            }
        }
    }
}