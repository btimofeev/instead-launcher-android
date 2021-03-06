/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead_api

import android.content.Context

interface InsteadApi {
    fun startGame(context: Context, gameName: String, playFromBeginning: Boolean = false)
}