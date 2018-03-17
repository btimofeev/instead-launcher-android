package org.emunix.insteadlauncher.ui.repository

import org.emunix.insteadlauncher.data.Game

interface RepositoryView {

    fun setLoadingIndicator(isActive: Boolean)

    fun showError(msg: String)

    fun showGames(games: List<Game>)

    fun showEmptyView(isActive: Boolean)

}