/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

import org.emunix.insteadlauncher.domain.model.GameModel

data class InstalledGame(
    val name: String,
    val title: String,
    val imageUrl: String,
)

fun List<GameModel>.toInstalledGame() =
    this.map { game ->
        InstalledGame(
            name = game.name,
            title = game.info.title,
            imageUrl = game.url.image,
        )
    }
