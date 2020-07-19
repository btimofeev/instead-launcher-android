/*
 * Copyright (c) 2018-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.ConsumableEvent
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.*
import org.emunix.insteadlauncher.services.ScanGames
import org.emunix.insteadlauncher.services.UpdateRepository
import java.io.IOException
import java.util.zip.ZipException


class RepositoryViewModel(var app: Application) : AndroidViewModel(app) {
    private val games = InsteadLauncher.db.games().observeAll()
    private val showProgress: MutableLiveData<Boolean> = MutableLiveData()
    private val showErrorView: MutableLiveData<Boolean> = MutableLiveData()
    private val showGameList: MutableLiveData<Boolean> = MutableLiveData()
    private val showInstallGameDialog: MutableLiveData<Boolean> = MutableLiveData()
    private val showSnackbar = MutableLiveData<ConsumableEvent<Int>>()
    private val showToast = MutableLiveData<ConsumableEvent<Int>>()

    private val viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    @SuppressLint("CheckResult")
    fun init() {
        RxBus.listen(UpdateRepoEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showErrorView.value = it.isError
                    showProgress.value = it.isLoading
                    showGameList.value = it.isGamesLoaded
                }

        if (app.isServiceRunning(UpdateRepository::class.java)) {
            showErrorView.value = false
            showProgress.value = true
            showGameList.value = false
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(app)
            val prefUpdateRepo = prefs.getBoolean("pref_update_repo_startup", false)
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
        UpdateRepository.start(app)
    }

    fun getGames(): LiveData<List<Game>> = games

    fun searchGames(query: String): LiveData<List<Game>> = InsteadLauncher.db.games().search(query)

    fun installGame(uri: Uri) = scope.launch {
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
        ScanGames.start(getApplication())
    }

    private suspend fun unzipGame(uri: Uri) {

        fun isGameZip(uri: Uri): Boolean {
            val inputStream = app.contentResolver.openInputStream(uri)
                    ?: throw IOException("inputStream is null")
            val isInsteadGameZip = GameHelper().isInsteadGameZip(inputStream)
            inputStream.close()
            return isInsteadGameZip
        }

        fun unzip(uri: Uri) {
            val inputStream = app.contentResolver.openInputStream(uri)
                    ?: throw IOException("inputStream is null")
            val gamesDir = InsteadLauncher.appComponent.storage().getGamesDirectory()
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
        super.onCleared()
        viewModelJob.cancel()
    }
}
