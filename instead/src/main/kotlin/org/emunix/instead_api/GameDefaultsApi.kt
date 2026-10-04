/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead_api

interface GameDefaultsApi {

    fun resolveTheme(): String

    fun resolveTextScale(): String

    fun resolveKeyboardButtonPosition(): String

    fun isTelevision(): Boolean
}
