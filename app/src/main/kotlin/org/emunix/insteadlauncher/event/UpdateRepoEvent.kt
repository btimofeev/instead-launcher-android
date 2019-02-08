package org.emunix.insteadlauncher.event

data class UpdateRepoEvent(
        val isLoading: Boolean,
        val isGamesLoaded: Boolean = false,
        val isError: Boolean = false,
        val message: String = ""
)
