/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

sealed interface SearchScreenState {

    data class Result(val games: List<RepoGame> = emptyList()) : SearchScreenState

    data object NothingFound : SearchScreenState

    data object Empty : SearchScreenState
}