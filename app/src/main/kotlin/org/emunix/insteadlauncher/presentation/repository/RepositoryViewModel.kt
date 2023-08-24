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
import kotlinx.coroutines.launch
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Error
import org.emunix.insteadlauncher.domain.model.UpdateGameListResult.Success
import org.emunix.insteadlauncher.domain.usecase.UpdateGameListUseCase
import org.emunix.insteadlauncher.helpers.ConsumableEvent
import org.emunix.insteadlauncher.helpers.gameparser.NotInsteadGameZipException
import org.emunix.insteadlauncher.manager.game.GameManager
import java.io.IOException
import java.util.zip.ZipException
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    private val gamesDB: GameDao,
    private val preferencesProvider: PreferencesProvider,
    private val gameManager: GameManager,
    private val updateGameListUseCase: UpdateGameListUseCase,
) : ViewModel() {

    private val games = gamesDB.observeAll()
    private val showProgress: MutableLiveData<Boolean> = MutableLiveData()
    private val showErrorView: MutableLiveData<Boolean> = MutableLiveData()
    private val showGameList: MutableLiveData<Boolean> = MutableLiveData()
    private val showInstallGameDialog: MutableLiveData<Boolean> = MutableLiveData()
    private val showSnackbar = MutableLiveData<ConsumableEvent<Int>>()
    private val showToast = MutableLiveData<ConsumableEvent<Int>>()

    fun init() {
        if (preferencesProvider.updateRepoWhenOpenRepositoryScreen) {
            updateRepository()
        }
    }

    fun getProgressState(): LiveData<Boolean> = showProgress

    fun getErrorViewState(): LiveData<Boolean> = showErrorView

    fun getGameListState(): LiveData<Boolean> = showGameList

    fun getInstallGameDialogState(): LiveData<Boolean> = showInstallGameDialog

    fun getSnackbarMessage(): LiveData<ConsumableEvent<Int>> = showSnackbar

    fun getToastMessage(): LiveData<ConsumableEvent<Int>> = showToast

    fun updateRepository() = viewModelScope.launch {
        showProgress.value = true
        showGameList.value = false
        showErrorView.value = false

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

    fun getGames(): LiveData<List<Game>> = games

    fun searchGames(query: String): LiveData<List<Game>> = gamesDB.search(query)

    fun installGame(uri: Uri) = viewModelScope.launch {
        showInstallGameDialog.value = true
        try {
            gameManager.installGameFromZip(uri)
        } catch (e: NotInsteadGameZipException) {
            showSnackbar.value = ConsumableEvent(string.error_not_instead_game_zip)
        } catch (e: ZipException) {
            showToast.value = ConsumableEvent(string.error_failed_to_unpack_zip)
        } catch (e: IOException) {
            showToast.value = ConsumableEvent(string.error_failed_to_unpack_zip)
        }
        showInstallGameDialog.value = false
        gameManager.scanGames()
    }
}
