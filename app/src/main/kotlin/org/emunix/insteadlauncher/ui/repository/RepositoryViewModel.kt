/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.GameDao
import org.emunix.insteadlauncher.helpers.ConsumableEvent
import org.emunix.insteadlauncher.data.model.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.helpers.gameparser.NotInsteadGameZipException
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.manager.game.GameManager
import org.emunix.insteadlauncher.manager.repository.RepositoryManager
import java.io.IOException
import java.util.zip.ZipException
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    private val eventBus: EventBus,
    private val gamesDB: GameDao,
    private val preferencesProvider: PreferencesProvider,
    private val gameManager: GameManager,
    private val repositoryManager: RepositoryManager,
) : ViewModel() {

    private val games = gamesDB.observeAll()
    private val showProgress: MutableLiveData<Boolean> = MutableLiveData()
    private val showErrorView: MutableLiveData<Boolean> = MutableLiveData()
    private val showGameList: MutableLiveData<Boolean> = MutableLiveData()
    private val showInstallGameDialog: MutableLiveData<Boolean> = MutableLiveData()
    private val showSnackbar = MutableLiveData<ConsumableEvent<Int>>()
    private val showToast = MutableLiveData<ConsumableEvent<Int>>()

    private var eventDisposable: Disposable? = null

    @SuppressLint("CheckResult")
    fun init() {
        eventDisposable = eventBus.listen(UpdateRepoEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showErrorView.value = it.isError
                showProgress.value = it.isLoading
                showGameList.value = it.isGamesLoaded
            }

        if (repositoryManager.isRepositoryUpdating()) {
            showErrorView.value = false
            showProgress.value = true
            showGameList.value = false
        } else {
            if (preferencesProvider.updateRepoWhenOpenRepositoryScreen) {
                updateRepository()
            }
        }
    }

    fun getProgressState(): LiveData<Boolean> = showProgress

    fun getErrorViewState(): LiveData<Boolean> = showErrorView

    fun getGameListState(): LiveData<Boolean> = showGameList

    fun getInstallGameDialogState(): LiveData<Boolean> = showInstallGameDialog

    fun getSnackbarMessage(): LiveData<ConsumableEvent<Int>> = showSnackbar

    fun getToastMessage(): LiveData<ConsumableEvent<Int>> = showToast

    fun updateRepository() {
        repositoryManager.updateRepository()
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

    override fun onCleared() {
        eventDisposable?.dispose()
        super.onCleared()
    }
}
