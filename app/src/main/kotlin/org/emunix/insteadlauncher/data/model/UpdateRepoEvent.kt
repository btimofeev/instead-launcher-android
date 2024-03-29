/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.model

data class UpdateRepoEvent(
    val isLoading: Boolean,
    val isGamesLoaded: Boolean = false,
    val isError: Boolean = false,
    val message: String = ""
)
