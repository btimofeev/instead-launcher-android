/*
 * Copyright (c) 2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

data class RepoScreenState(
    val games: List<RepoGame> = emptyList(),
    val updateRepo: UpdateRepoState = UpdateRepoState.HIDDEN,
    val installGameProgress: Boolean = false,
    val errorMessage: String? = null,
)

enum class UpdateRepoState {

    HIDDEN,

    UPDATING,

    ERROR,
}