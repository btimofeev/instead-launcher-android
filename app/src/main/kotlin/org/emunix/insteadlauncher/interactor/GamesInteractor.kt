/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.interactor

import android.net.Uri

interface GamesInteractor {

    /**
     * Run game
     *
     * @param gameName technical name of the game (name of the directory with the game)
     * @param playFromBeginning if true start the game from the beginning, if false then the game will load autosave
     */
    fun startGame(gameName: String, playFromBeginning: Boolean = false)

    /**
     * Install game
     *
     * @param gameName technical name of the game (name of the directory with the game)
     * @param gameUrl url from which the archive with the game will be loaded
     * @param gameTitle game name to display in notification
     */
    fun installGame(gameName: String, gameUrl: String, gameTitle: String)

    /**
     * Install local game from zip file
     *
     * @param uri [Uri] to zip file
     */
    suspend fun installGameFromZip(uri: Uri)

    /**
     * Delete game
     *
     * @param gameName technical name of the game (name of the directory with the game)
     */
    fun deleteGame(gameName: String)

    /**
     * Scan the games directory.
     * Searches for locally installed games and games that have been removed and updates the information in the database
     */
    fun scanGames()

    /**
     * Fetch game list from network repository
     */
    fun updateRepository()

    /**
     * Find out if the process of updating the repository is currently running
     */
    fun isRepositoryUpdating(): Boolean
}