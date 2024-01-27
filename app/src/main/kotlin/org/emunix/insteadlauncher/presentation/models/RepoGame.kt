/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

import org.emunix.insteadlauncher.domain.model.GameModel

data class RepoGame(
    val name: String,
    val title: String,
    val imageUrl: String,
    val description: String,
    val isHasNewVersion: Boolean,
)

fun List<GameModel>.toRepoGames() =
    this.map { game ->
        RepoGame(
            name = game.name,
            title = game.info.title,
            imageUrl = game.url.image,
            description = game.info.shortDescription,
            isHasNewVersion = game.version.installed.isNotBlank() and (game.version.availableOnSite != game.version.installed)
        )
    }
