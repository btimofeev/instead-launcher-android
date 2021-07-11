/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.instead_api.InsteadApi
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.GameDao
import org.emunix.insteadlauncher.event.ConsumableEvent
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.*
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.helpers.gameparser.GameParserImpl
import org.emunix.insteadlauncher.helpers.gameparser.NotInsteadGameZipException
import org.emunix.insteadlauncher.services.ScanGames
import org.emunix.insteadlauncher.services.UpdateRepository
import java.io.IOException
import java.util.zip.ZipException
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventBus: EventBus,
    private val gamesDB: GameDao,
    private val preferences: SharedPreferences,
    private val storage: Storage
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

        if (context.isServiceRunning(UpdateRepository::class.java)) {
            showErrorView.value = false
            showProgress.value = true
            showGameList.value = false
        } else {
            val prefUpdateRepo = preferences.getBoolean("pref_update_repo_startup", false)
            if (prefUpdateRepo) {
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
        UpdateRepository.start(context)
    }

    fun getGames(): LiveData<List<Game>> = games

    fun searchGames(query: String): LiveData<List<Game>> = gamesDB.search(query)

    fun installGame(uri: Uri) = viewModelScope.launch {
        showInstallGameDialog.value = true
        try {
            unzipGame(uri)
        } catch (e: NotInsteadGameZipException) {
            showSnackbar.value = ConsumableEvent(R.string.error_not_instead_game_zip)
        } catch (e: ZipException) {
            showToast.value = ConsumableEvent(R.string.error_failed_to_unpack_zip)
        } catch (e: IOException) {
            showToast.value = ConsumableEvent(R.string.error_failed_to_unpack_zip)
        }
        showInstallGameDialog.value = false
        ScanGames.start(context)
    }

    private suspend fun unzipGame(uri: Uri) {

        fun isGameZip(uri: Uri): Boolean {
            val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("inputStream is null")
            val isInsteadGameZip = GameParserImpl().isInsteadGameZip(inputStream)
            inputStream.close()
            return isInsteadGameZip
        }

        fun unzip(uri: Uri) {
            val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("inputStream is null")
            val gamesDir = storage.getGamesDirectory()
            inputStream.unzip(gamesDir)
            inputStream.close()
        }

        withContext(Dispatchers.IO) {
            if (isGameZip(uri)) {
                unzip(uri)
            } else
                throw NotInsteadGameZipException("main.lua not found")
        }
    }

    override fun onCleared() {
        eventDisposable?.dispose()
        super.onCleared()
    }
}
