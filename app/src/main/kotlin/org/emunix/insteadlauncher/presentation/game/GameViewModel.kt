/*
 * Copyright (c) 2018, 2020-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.game

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Error
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Success
import org.emunix.insteadlauncher.domain.usecase.GetDownloadGamesStatusUseCase
import org.emunix.insteadlauncher.helpers.ConsumableEvent
import org.emunix.insteadlauncher.helpers.getDownloadingMessage
import org.emunix.insteadlauncher.helpers.resourceprovider.ResourceProvider
import org.emunix.insteadlauncher.manager.game.GameManager
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gamesDB: GameDao,
    private val gameManager: GameManager,
    private val resourceProvider: ResourceProvider,
    private val getDownloadGamesStatusUseCase: GetDownloadGamesStatusUseCase,
) : ViewModel() {

    private lateinit var game: LiveData<Game>
    private val progress: MutableLiveData<Int> = MutableLiveData()
    private val progressMessage: MutableLiveData<String> = MutableLiveData()
    private val errorMessage: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()

    @SuppressLint("CheckResult")
    fun init(gameName: String) {
        game = gamesDB.observeByName(gameName)
        observeDownloadStatus(gameName)
    }

    fun installGame() {
        val gameToInstall = game.value
        if (gameToInstall != null) {
            gameManager.installGame(gameToInstall.name, gameToInstall.url, gameToInstall.title)
        }
    }

    fun runGame() {
        val gameToRun = game.value
        if (gameToRun != null && gameToRun.state == Game.State.INSTALLED) {
            gameManager.startGame(gameToRun.name)
        }
    }

    fun getProgress(): LiveData<Int> = progress

    fun getGame(): LiveData<Game> = game

    fun getProgressMessage(): LiveData<String> = progressMessage

    fun getErrorMessage(): LiveData<ConsumableEvent<String>> = errorMessage

    private fun observeDownloadStatus(gameName: String) = viewModelScope.launch {
        getDownloadGamesStatusUseCase()
            .filter { it.gameName == gameName }
            .collect { downloadStatus ->
                when (downloadStatus) {
                    is Downloading -> {
                        progress.value = downloadStatus.downloadedInPercentage
                        progressMessage.value = downloadStatus.getDownloadingMessage(resourceProvider)
                    }
                    is Success -> {
                        progress.value = -1
                        progressMessage.value = resourceProvider.getString(R.string.game_activity_message_installing)
                    }
                    is Error -> {
                        errorMessage.value = ConsumableEvent(downloadStatus.errorMessage)
                    }
                }
            }
    }
}
