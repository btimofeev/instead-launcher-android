package org.emunix.insteadlauncher.ui.repository

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.RxBus
import org.emunix.insteadlauncher.services.UpdateRepository


class RepositoryViewModel(var app: Application) : AndroidViewModel(app) {
    private val games = InsteadLauncher.db.games().observeAll()
    private var showProgress: MutableLiveData<Boolean> = MutableLiveData()
    private var showErrorView: MutableLiveData<Boolean> = MutableLiveData()
    private var showGameList: MutableLiveData<Boolean> = MutableLiveData()

    @SuppressLint("CheckResult")
    fun init() {
        RxBus.listen(UpdateRepoEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showErrorView.value = it.isError
                    showProgress.value = it.isLoading
                    showGameList.value = it.isGamesLoaded
                }
    }

    fun getProgressState(): LiveData<Boolean> = showProgress

    fun getErrorViewState(): LiveData<Boolean> = showErrorView

    fun getGameListState(): LiveData<Boolean> = showGameList

    fun updateRepository() {
        val updateRepoIntent = Intent(app, UpdateRepository::class.java)
        app.startService(updateRepoIntent)
    }

    fun getGames(): LiveData<List<Game>> = games
}
