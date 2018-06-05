package org.emunix.insteadlauncher.event

data class UpdateRepoEvent(
        val action: Boolean,
        val error: Boolean = false,
        val message: String = ""
)
