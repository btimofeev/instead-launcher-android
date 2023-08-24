/*
 * Copyright (c) 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.models

import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider

data class GameInfo(
    val name: String,
    val title: String,
    val author: String,
    val description: String,
    val version: String,
    val size: String,
    val imageUrl: String,
    val siteUrl: String,
    val state: GameState,
    val isUpdateButtonShow: Boolean,
)

fun GameModel.toGameInfo(resourceProvider: ResourceProvider) =
    GameInfo(
        name = this.name,
        title = this.info.title,
        author = this.info.author,
        description = this.info.description,
        version = this.getVersionText(resourceProvider),
        size = this.getSizeText(resourceProvider),
        imageUrl = this.url.image,
        siteUrl = this.url.site,
        state = this.state,
        isUpdateButtonShow = this.state == INSTALLED && this.version.availableOnSite != this.version.installed
    )

private fun GameModel.getVersionText(resourceProvider: ResourceProvider) =
    if (this.version.installed.isNotBlank() and (this.version.availableOnSite != this.version.installed)) {
        resourceProvider.getString(
            R.string.game_activity_label_version,
            this.version.installed + " (\u2191${this.version.availableOnSite})"
        )
    } else {
        resourceProvider.getString(R.string.game_activity_label_version, this.version.availableOnSite)
    }

private fun GameModel.getSizeText(resourceProvider: ResourceProvider) =
    resourceProvider.getString(R.string.game_activity_label_size, FileUtils.byteCountToDisplaySize(this.info.gameSize))