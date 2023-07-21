/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.model

data class GameModel(
    val name: String,
    val info: GameInfo,
    val url: GameUrl,
    val version: GameVersion,
    val state: GameState,
) {

    val isInstalledFromSite: Boolean = url.site.isNotBlank()
}

data class GameInfo(
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val shortDescription: String = "",
    val lastReleaseDate: String = "",
    val gameSize: Long = 0,
    val lang: String = "",
)

data class GameVersion(
    val installed: String = "",
    val availableOnSite: String = "",
)

data class GameUrl(
    val image: String = "",
    val site: String = "",
    val download: String = "",
)

enum class GameState {
    NO_INSTALLED,
    INSTALLED,
    IS_INSTALL,
    IS_DELETE,
    IS_UPDATE,
    IN_QUEUE_TO_INSTALL
}