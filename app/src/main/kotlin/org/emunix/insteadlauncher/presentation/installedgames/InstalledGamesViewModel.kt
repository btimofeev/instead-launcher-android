/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.domain.usecase.GetGamesFlowUseCase
import org.emunix.insteadlauncher.domain.usecase.StartUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.domain.usecase.StopUpdateRepositoryWorkUseCase
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.models.InstalledGame
import org.emunix.insteadlauncher.presentation.models.toInstalledGame
import javax.inject.Inject

@HiltViewModel
class InstalledGamesViewModel @Inject constructor(
    private val gameManager: GameManager,
    private val getGamesFlowUseCase: GetGamesFlowUseCase,
    private val preferencesProvider: PreferencesProvider,
    private val startUpdateRepositoryWorkUseCase: StartUpdateRepositoryWorkUseCase,
    private val stopUpdateRepositoryWorkUseCase: StopUpdateRepositoryWorkUseCase
) : ViewModel() {

    private val _gameItems = MutableStateFlow<List<InstalledGame>>(emptyList())

    val gameItems: StateFlow<List<InstalledGame>> = _gameItems.asStateFlow()

    fun init() {
        startUpdateRepoWorker()
        observeGames()
    }

    fun playGame(gameName: String, playFromBeginning: Boolean = false) {
        gameManager.startGame(gameName, playFromBeginning)
    }

    private fun observeGames() = viewModelScope.launch {
        getGamesFlowUseCase(onlyInstalled = true)
            .collect { _gameItems.value = it.toInstalledGame() }
    }

    private fun startUpdateRepoWorker() {
        val updatePref = preferencesProvider.updateRepoInBackground

        if (updatePref) {
            startUpdateRepositoryWorkUseCase()
        } else {
            stopUpdateRepositoryWorkUseCase()
        }
    }
}
