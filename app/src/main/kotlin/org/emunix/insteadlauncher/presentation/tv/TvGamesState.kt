/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import org.emunix.insteadlauncher.presentation.models.UpdateRepoState

data class TvGamesState(
    val installedGames: List<TvGameItem> = emptyList(),
    val catalogGames: List<TvGameItem> = emptyList(),
    val updateRepo: UpdateRepoState = UpdateRepoState.HIDDEN,
    val errorMessage: String? = null,
)

data class TvGameItem(
    val name: String,
    val title: String,
    val imageUrl: String,
)
