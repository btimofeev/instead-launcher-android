/*
 * Copyright (c) 2018, 2020-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Error
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.domain.usecase.GetGameInfoFlowUseCase
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.models.DownloadError
import org.emunix.insteadlauncher.presentation.models.DownloadState
import org.emunix.insteadlauncher.presentation.models.GameInfo
import org.emunix.insteadlauncher.presentation.models.toGameInfo
import org.emunix.insteadlauncher.utils.getDownloadingMessage
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getGameInfoFlowUseCase: GetGameInfoFlowUseCase,
    private val getDownloadGamesStatusUseCase: GetDownloadGamesStatusUseCase,
    private val gameManager: GameManager,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _game = MutableStateFlow<GameInfo?>(null)
    private val _downloadState = MutableStateFlow<DownloadState?>(null)
    private val _downloadErrorCommand = Channel<DownloadError>()
    private val _closeScreenCommand = Channel<Unit>()

    val game: StateFlow<GameInfo?> = _game.asStateFlow()
    val downloadState: StateFlow<DownloadState?> = _downloadState.asStateFlow()
    val downloadErrorCommand = _downloadErrorCommand.receiveAsFlow()
    val closeScreenCommand = _closeScreenCommand.receiveAsFlow()

    private var gameModel: GameModel? = null

    fun init(gameName: String) {
        observeGameInfo(gameName)
        observeDownloadStatus(gameName)
    }

    fun installGame() {
        gameModel?.let {
            gameManager.installGame(it.name, it.url.download, it.info.title)
        }
    }

    fun runGame() {
        val gameToRun = game.value
        if (gameToRun != null && gameToRun.state == INSTALLED) {
            gameManager.startGame(gameToRun.name)
        }
    }

    private fun observeGameInfo(gameName: String) = viewModelScope.launch {
        getGameInfoFlowUseCase(gameName)
            .collect { game ->
                if (game == null) {
                    _closeScreenCommand.send(Unit)
                } else {
                    gameModel = game
                    _game.value = game.toGameInfo(resourceProvider)
                }
            }
    }

    private fun observeDownloadStatus(gameName: String) = viewModelScope.launch {
        getDownloadGamesStatusUseCase()
            .filter { it.gameName == gameName }
            .collect { downloadStatus ->
                when (downloadStatus) {
                    is Downloading -> {
                        _downloadState.value = DownloadState(
                            progress = downloadStatus.downloadedInPercentage,
                            message = downloadStatus.getDownloadingMessage(resourceProvider)
                        )
                    }

                    is Success -> {
                        _downloadState.value = DownloadState(
                            progress = -1,
                            message = resourceProvider.getString(R.string.game_activity_message_installing)
                        )
                    }

                    is Error -> {
                        _downloadErrorCommand.trySend(
                            DownloadError(
                                message = downloadStatus.errorMessage
                            )
                        )
                    }
                }
            }
    }
}
