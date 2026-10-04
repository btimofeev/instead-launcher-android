/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead_api

interface GameDefaultsApi {

    fun resolveTheme(): String

    fun resolveTextScale(): String

    fun resolveKeyboardButtonPosition(): String

    /**
     * INSTEAD keyboard mode to force for this device: 0 smart, 1 links, 2 scroll,
     * empty string leaves the mode saved by the game alone.
     */
    fun resolveKeyboardMode(): String

    fun isTelevision(): Boolean
}
