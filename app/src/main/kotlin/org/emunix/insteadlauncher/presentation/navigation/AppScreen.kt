/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {

    @Serializable
    data object UnpackResourcesScreen : AppScreen

    @Serializable
    data object InstalledGamesScreen : AppScreen

    @Serializable
    data object SettingsScreen: AppScreen

    @Serializable
    data object AboutAppScreen: AppScreen

    @Serializable
    data object RepositoryScreen: AppScreen

    @Serializable
    data object SearchScreen: AppScreen

    @Serializable
    data class GameInfoScreen(val gameName: String): AppScreen
}