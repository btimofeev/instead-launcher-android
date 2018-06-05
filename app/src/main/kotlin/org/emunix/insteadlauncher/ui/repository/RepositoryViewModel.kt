package org.emunix.insteadlauncher.ui.repository

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import io.reactivex.android.schedulers.AndroidSchedulers
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.UpdateRepoEvent
import org.emunix.insteadlauncher.helpers.RxBus
import org.emunix.insteadlauncher.helpers.showToast
import org.emunix.insteadlauncher.services.UpdateRepository


class RepositoryViewModel(var app: Application) : AndroidViewModel(app) {
    private var showProgress: MutableLiveData<Boolean> = MutableLiveData()

    fun init() {
        RxBus.listen(UpdateRepoEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.error) {
                        app.showToast(it.message)
                    }
                    showProgress.setValue(it.action)
                })
    }

    fun getProgressState(): LiveData<Boolean> = showProgress

    fun updateRepository() {
        val updateRepoIntent = Intent(app, UpdateRepository::class.java)
        app.startService(updateRepoIntent)
    }

    fun getGames(): LiveData<List<Game>> = InsteadLauncher.gamesDB.gameDao().getAll()
}