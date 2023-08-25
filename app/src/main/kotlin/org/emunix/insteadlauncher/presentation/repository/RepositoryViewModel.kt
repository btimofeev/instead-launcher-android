/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.domain.model.NotInsteadGameZipException
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Error
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Success
import org.emunix.insteadlauncher.domain.usecase.GetGamesFlowUseCase
import org.emunix.insteadlauncher.domain.usecase.SearchGamesUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCase
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.models.ErrorDialogModel
import org.emunix.insteadlauncher.presentation.models.RepoGame
import org.emunix.insteadlauncher.presentation.models.toRepoGames
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    private val getGamesFlowUseCase: GetGamesFlowUseCase,
    private val preferencesProvider: PreferencesProvider,
    private val gameManager: GameManager,
    private val updateGameListUseCase: UpdateGameListUseCase,
    private val searchGamesUseCase: SearchGamesUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _gameItems = MutableStateFlow<List<RepoGame>>(emptyList())
    private val _showSearchNotFoundError = MutableStateFlow(false)
    private val _showErrorDialog = Channel<ErrorDialogModel>()

    val gameItems: StateFlow<List<RepoGame>> = _gameItems.asStateFlow()
    val showSearchNotFoundError: StateFlow<Boolean> = _showSearchNotFoundError.asStateFlow()
    val showErrorDialog = _showErrorDialog.receiveAsFlow()

    private val showProgress: MutableLiveData<Boolean> = MutableLiveData()
    private val showErrorView: MutableLiveData<Boolean> = MutableLiveData()
    private val showGameList: MutableLiveData<Boolean> = MutableLiveData()
    private val showInstallGameDialog: MutableLiveData<Boolean> = MutableLiveData()

    fun init() {
        if (preferencesProvider.updateRepoWhenOpenRepositoryScreen) {
            updateRepository()
        }
        observeGames()
    }

    fun getProgressState(): LiveData<Boolean> = showProgress

    fun getErrorViewState(): LiveData<Boolean> = showErrorView

    fun getGameListState(): LiveData<Boolean> = showGameList

    fun getInstallGameDialogState(): LiveData<Boolean> = showInstallGameDialog

    fun updateRepository() = viewModelScope.launch {
        showProgress.value = true
        showGameList.value = false
        showErrorView.value = false
        _showSearchNotFoundError.value = false

        when (updateGameListUseCase()) {
            is Success -> {
                showGameList.value = true
                showProgress.value = false
            }

            is Error -> {
                showProgress.value = false
                showErrorView.value = true
            }
        }
    }

    fun searchGames(query: String) = viewModelScope.launch {
        val games = searchGamesUseCase(query)
        _gameItems.value = games.toRepoGames()
        _showSearchNotFoundError.value = games.isEmpty()
    }

    fun installGame(uri: Uri) = viewModelScope.launch {
        showInstallGameDialog.value = true
        try {
            gameManager.installGameFromZip(uri)
        } catch (e: NotInsteadGameZipException) {
            showErrorDialog(text = resourceProvider.getString(string.error_not_instead_game_zip))
        } catch (e: Throwable) {
            showErrorDialog(text = resourceProvider.getString(string.error_failed_to_unpack_zip))
        }
        showInstallGameDialog.value = false
        gameManager.scanGames()
    }

    private fun observeGames() = viewModelScope.launch {
        getGamesFlowUseCase()
            .collect { games ->
                _gameItems.value = games
                    .sortedByDescending { it.info.lastReleaseDate }
                    .toRepoGames()
            }
    }

    private fun showErrorDialog(text: String) {
        _showErrorDialog.trySend(
            ErrorDialogModel(
                title = resourceProvider.getString(string.error),
                message = text
            )
        )
    }
}
