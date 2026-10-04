/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.NotInsteadGameZipException
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult
import org.emunix.insteadlauncher.domain.usecase.GetGamesFlowUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCase
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.presentation.models.UpdateRepoState
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import timber.log.Timber
import java.util.zip.ZipException
import javax.inject.Inject

@HiltViewModel
class TvGamesViewModel @Inject constructor(
    private val getGamesFlowUseCase: GetGamesFlowUseCase,
    private val updateGameListUseCase: UpdateGameListUseCase,
    private val gameManager: GameManager,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(TvGamesState())
    val state = _state.asStateFlow()

    var lastOpenedGameKey: String? = null
        private set

    var lastOpenedGameIndex: Int = -1
        private set

    fun rememberOpenedGame(key: String, index: Int) {
        lastOpenedGameKey = key
        lastOpenedGameIndex = index
    }

    private var initialized = false

    fun init() {
        if (initialized) {
            return
        }
        initialized = true
        observeGames()
        updateRepository()
    }

    fun updateRepository() = viewModelScope.launch {
        _state.update { it.copy(updateRepo = UpdateRepoState.UPDATING) }
        when (val result = updateGameListUseCase()) {
            is UpdateGameListResult.Success -> {
                _state.update { it.copy(updateRepo = UpdateRepoState.HIDDEN) }
            }

            is UpdateGameListResult.Error -> {
                Timber.e(result.e)
                _state.update { it.copy(updateRepo = UpdateRepoState.ERROR) }
            }
        }
    }

    fun installGameFromZip(uri: Uri) = viewModelScope.launch {
        try {
            gameManager.installGameFromZip(uri)
        } catch (e: NotInsteadGameZipException) {
            showError(resourceProvider.getString(R.string.error_not_instead_game_zip))
        } catch (e: ZipException) {
            Timber.e(e)
            showError(resourceProvider.getString(R.string.error_corrupted_zip))
        } catch (e: Throwable) {
            Timber.e(e)
            showError(resourceProvider.getString(R.string.error_failed_to_install_zip))
        }
        gameManager.scanGames()
    }

    fun onErrorDismissed() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    private fun observeGames() = viewModelScope.launch {
        getGamesFlowUseCase().collect { games ->
            _state.update { state ->
                state.copy(
                    installedGames = games
                        .filter { it.state == GameState.INSTALLED }
                        .sortedBy { it.info.title.lowercase() }
                        .toTvGameItems(),
                    catalogGames = games
                        .filter { it.state == GameState.NO_INSTALLED }
                        .sortedByDescending { it.info.lastReleaseDate }
                        .toTvGameItems(),
                )
            }
        }
    }

    private fun List<GameModel>.toTvGameItems() =
        map { game ->
            TvGameItem(
                name = game.name,
                title = game.info.title,
                imageUrl = game.url.image,
            )
        }
}
