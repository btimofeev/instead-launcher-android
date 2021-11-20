/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead_api

interface InsteadApi {

    /**
     * Run game
     *
     * @param gameName technical name of the game (name of the directory with the game)
     * @param playFromBeginning if true start the game from the beginning, if false then the game will load autosave
     */
    fun startGame(gameName: String, playFromBeginning: Boolean = false)
}