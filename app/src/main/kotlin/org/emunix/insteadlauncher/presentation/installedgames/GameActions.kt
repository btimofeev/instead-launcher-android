package org.emunix.insteadlauncher.presentation.installedgames

data class GameActions(
    val onPlayClick: (gameName: String) -> Unit,
    val onPlayFromBeginningClick: (gameName: String) -> Unit,
    val onDeleteClick: (gameName: String) -> Unit,
    val onAboutClick: (gameName: String) -> Unit,
)