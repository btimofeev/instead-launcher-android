/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import kotlinx.serialization.Serializable

@Serializable
sealed interface TvScreen {

    @Serializable
    data object UnpackResourcesScreen : TvScreen

    @Serializable
    data object GamesScreen : TvScreen

    @Serializable
    data class GameInfoScreen(val gameName: String) : TvScreen

    @Serializable
    data object SearchScreen : TvScreen

    @Serializable
    data object SettingsScreen : TvScreen

    @Serializable
    data object AboutScreen : TvScreen
}
