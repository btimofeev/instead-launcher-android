package org.emunix.insteadlauncher.ui.repository

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.services.UpdateRepository

class RepositoryViewModel(var app: Application) : AndroidViewModel(app) {
    private var showProgress: MutableLiveData<Boolean> = MutableLiveData()

    fun getProgressState(): LiveData<Boolean> = showProgress

    fun updateRepository() {
        val updateRepoIntent = Intent(app, UpdateRepository::class.java)
        app.startService(updateRepoIntent)
    }

    fun getGames(): LiveData<List<Game>> = InsteadLauncher.gamesDB.gameDao().getAll()
}