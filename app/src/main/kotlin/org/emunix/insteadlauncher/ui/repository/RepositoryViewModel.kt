/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
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
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.Event
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.*
import org.emunix.insteadlauncher.services.UpdateRepository
import org.emunix.insteadlauncher.services.ScanGames
import java.io.IOException
import java.util.zip.ZipException


class RepositoryViewModel(var app: Application) : AndroidViewModel(app) {
    private val games = InsteadLauncher.db.games().observeAll()
    private var showProgress: MutableLiveData<Boolean> = MutableLiveData()
    private var showErrorView: MutableLiveData<Boolean> = MutableLiveData()
    private var showGameList: MutableLiveData<Boolean> = MutableLiveData()
    private var showInstallGameDialog: MutableLiveData<Boolean> = MutableLiveData()
    private var showSnackbar = MutableLiveData<Event<String>>()

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

    fun getSnackbarMessage(): LiveData<Event<String>> = showSnackbar

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
            showSnackbar.value = Event(getApplication<InsteadLauncher>().getString(R.string.error_not_instead_game_zip))
        } catch (e: ZipException) {
            getApplication<InsteadLauncher>().showToast(getApplication<InsteadLauncher>().getString(R.string.error_failed_to_unpack_zip))
        } catch (e: IOException) {
            getApplication<InsteadLauncher>().showToast(getApplication<InsteadLauncher>().getString(R.string.error_failed_to_unpack_zip))
        }
        showInstallGameDialog.value = false
        ScanGames.start(getApplication())
    }

    private suspend fun unzipGame(uri: Uri) {
        withContext(Dispatchers.IO) {
            var inputStream = getApplication<InsteadLauncher>().applicationContext.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                if (!GameHelper().isInsteadGameZip(inputStream)) {
                    inputStream.close()
                    throw NotInsteadGameZipException("main.lua not found")
                }
                inputStream.close()
            }

            inputStream = getApplication<InsteadLauncher>().applicationContext.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val gamesDir = StorageHelper(getApplication()).getGamesDirectory()
                inputStream.unzip(gamesDir)
                inputStream.close()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
