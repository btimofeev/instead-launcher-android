/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.mapper

import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.Game.State
import org.emunix.insteadlauncher.domain.model.GameInfo
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.GameUrl
import org.emunix.insteadlauncher.domain.model.GameVersion

fun Game.toDomain(): GameModel =
    GameModel(
        name = this.name,
        info = GameInfo(
            title = this.title,
            author = this.author,
            description = this.description,
            shortDescription = this.brief,
            lastReleaseDate = this.date,
            gameSize = this.size,
            lang = this.lang,
        ),
        url = GameUrl(
            image = this.image,
            site = this.descurl,
            download = this.url,
        ),
        version = GameVersion(
            installed = this.installedVersion,
            availableOnSite = this.version,
        ),
        state = GameState.valueOf(this.state.name)
    )

fun GameModel.toData(): Game = Game(
    name = this.name,
    title = this.info.title,
    author = this.info.author,
    date = this.info.lastReleaseDate,
    version = this.version.availableOnSite,
    size = this.info.gameSize,
    url = this.url.download,
    image = this.url.image,
    lang = this.info.lang,
    description = this.info.description,
    descurl = this.url.site,
    brief = this.info.shortDescription,
    installedVersion = this.version.installed,
    state = State.valueOf(this.state.name)
)