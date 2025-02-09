/*
 * Copyright (c) 2018, 2020-2021, 2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Error
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.domain.usecase.GetGameInfoFlowUseCase
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.models.DownloadError
import org.emunix.insteadlauncher.presentation.models.GameInfoScreenState
import org.emunix.insteadlauncher.presentation.models.ProgressType
import org.emunix.insteadlauncher.presentation.models.toGameInfoScreenState
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

    private val _state = MutableStateFlow(GameInfoScreenState())
    private val _closeScreenCommand = Channel<Unit>()
    private val _showDeleteGameDialog = MutableStateFlow<String?>(null)
    private val _showErrorDialog = MutableStateFlow<DownloadError?>(null)

    val state = _state.asStateFlow()
    val closeScreenCommand = _closeScreenCommand.receiveAsFlow()
    val showDeleteGameDialog = _showDeleteGameDialog.asStateFlow()
    val showErrorDialog = _showErrorDialog.asStateFlow()

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
        val gameToRun = _state.value
        if (gameToRun.name.isNotBlank() && gameToRun.state == INSTALLED) {
            gameManager.startGame(gameToRun.name)
        }
    }

    fun onDeleteGameClicked() {
        _showDeleteGameDialog.value = gameModel?.name
    }

    fun onDeleteGameConfirmed(gameName: String) {
        gameManager.deleteGame(gameName)
        _showDeleteGameDialog.value = null
    }

    fun onDeleteGameRejected() {
        _showDeleteGameDialog.value = null
    }

    fun onErrorDialogDismissed() {
        _showErrorDialog.value = null
    }

    private fun observeGameInfo(gameName: String) = viewModelScope.launch {
        getGameInfoFlowUseCase(gameName)
            .collect { game ->
                if (game == null) {
                    _closeScreenCommand.send(Unit)
                } else {
                    gameModel = game
                    _state.update { game.toGameInfoScreenState(resourceProvider) }
                }
            }
    }

    private fun observeDownloadStatus(gameName: String) = viewModelScope.launch {
        getDownloadGamesStatusUseCase()
            .filter { it.gameName == gameName }
            .collect { downloadStatus ->
                handleStatus(downloadStatus)
            }
    }

    private fun handleStatus(downloadStatus: DownloadGameStatus) {
        when (downloadStatus) {
            is Downloading -> {
                _state.update {
                    it.copy(
                        progressMessage = downloadStatus.getDownloadingMessage(resourceProvider),
                        progress = ProgressType.WithValue(
                            value = convertToRangeFromZeroToOne(downloadStatus.downloadedInPercentage)
                        )
                    )
                }
            }

            is Success -> {
                _state.update {
                    it.copy(
                        progressMessage = resourceProvider.getString(R.string.game_activity_message_installing),
                        progress = ProgressType.Indeterminate,
                    )
                }
            }

            is Error -> {
                _showErrorDialog.value = DownloadError(
                    message = downloadStatus.errorMessage
                )
            }
        }
    }

    /**
     * Convert int range [1 .. 100] to float range [0.1 .. 1]
     */
    private fun convertToRangeFromZeroToOne(value: Int): Float =
        0.1f + ((0.9f * (value - 1)) / 99)
}
