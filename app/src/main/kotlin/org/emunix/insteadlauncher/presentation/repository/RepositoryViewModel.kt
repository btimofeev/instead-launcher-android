/*
 * Copyright (c) 2018-2021, 2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.domain.model.NotInsteadGameZipException
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Error
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Success
import org.emunix.insteadlauncher.domain.usecase.GetGamesFlowUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCase
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.models.ErrorDialogModel
import org.emunix.insteadlauncher.presentation.models.RepoScreenState
import org.emunix.insteadlauncher.presentation.models.UpdateRepoState
import org.emunix.insteadlauncher.presentation.models.toRepoGames
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    private val getGamesFlowUseCase: GetGamesFlowUseCase,
    private val preferencesProvider: PreferencesProvider,
    private val gameManager: GameManager,
    private val updateGameListUseCase: UpdateGameListUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(RepoScreenState())
    private val _showErrorDialog = MutableStateFlow<ErrorDialogModel?>(null)

    val state = _state.asStateFlow()
    val showErrorDialog = _showErrorDialog.asStateFlow()

    fun init() {
        if (preferencesProvider.updateRepoWhenOpenRepositoryScreen) {
            updateRepository()
        }
        observeGames()
    }

    fun updateRepository() = viewModelScope.launch {
        _state.update { it.copy(updateRepo = UpdateRepoState.UPDATING) }

        when (val result = updateGameListUseCase()) {
            is Success -> {
                _state.update { it.copy(updateRepo = UpdateRepoState.HIDDEN) }
            }

            is Error -> {
                Timber.e(result.e)
                _state.update { it.copy(updateRepo = UpdateRepoState.ERROR) }
            }
        }
    }

    fun installGame(uri: Uri) = viewModelScope.launch {
        _state.update { it.copy(installGameProgress = true) }
        try {
            gameManager.installGameFromZip(uri)
        } catch (e: NotInsteadGameZipException) {
            showErrorDialog(text = resourceProvider.getString(string.error_not_instead_game_zip))
        } catch (e: Throwable) {
            showErrorDialog(text = resourceProvider.getString(string.error_failed_to_unpack_zip))
        }
        _state.update { it.copy(installGameProgress = false) }
        gameManager.scanGames()
    }

    fun onErrorDialogDismiss() {
        _showErrorDialog.value = null
    }

    private fun observeGames() = viewModelScope.launch {
        getGamesFlowUseCase()
            .collect { games ->
                _state.update { prev ->
                    prev.copy(
                        games = games
                            .sortedByDescending { it.info.lastReleaseDate }
                            .toRepoGames()
                    )
                }
            }
    }

    private fun showErrorDialog(text: String) {
        _showErrorDialog.value = ErrorDialogModel(
            title = resourceProvider.getString(string.error),
            message = text
        )
    }
}
