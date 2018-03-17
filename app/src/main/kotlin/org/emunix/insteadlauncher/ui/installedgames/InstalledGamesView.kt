package org.emunix.insteadlauncher.ui.installedgames

import org.emunix.insteadlauncher.data.Game

interface InstalledGamesView {

    fun setLoadingIndicator(isActive: Boolean)

    fun showError(msg: String)

    fun showGames(games: List<Game>)

    fun showEmptyView(isActive: Boolean)

}
