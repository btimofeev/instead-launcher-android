/*
 * Copyright (c) 2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead.utils

/**
 * Keycode handler
 */
interface OnKeyCodeHandler {

    /**
     * Key was pressed
     *
     * @param keyCode code of the pressed key
     */
    fun onKeyDown(keyCode: Int)

    /**
     * Key was released
     *
     * @param keyCode released key code
     */
    fun onKeyUp(keyCode: Int)
}