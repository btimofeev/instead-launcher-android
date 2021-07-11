/*
 * Copyright (c) 2018, 2020-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.game

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.emunix.instead_api.InsteadApi
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.GameDao
import org.emunix.insteadlauncher.event.ConsumableEvent
import org.emunix.insteadlauncher.event.DownloadProgressEvent
import org.emunix.insteadlauncher.helpers.GameDbHelper
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.services.InstallGame
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventBus: EventBus,
    private val instead: InsteadApi,
    private val gamesDB: GameDao,
    private val gamesDbHelper: GameDbHelper
) : ViewModel() {

    private lateinit var game: LiveData<Game>
    private val progress: MutableLiveData<Int> = MutableLiveData()
    private val progressMessage: MutableLiveData<String> = MutableLiveData()
    private val errorMessage: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()
    private var eventDisposable: Disposable? = null

    @SuppressLint("CheckResult")
    fun init(gameName: String) {
        game = gamesDB.observeByName(gameName)

        eventDisposable = eventBus.listen(DownloadProgressEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.gameName == gameName) {
                        if (it.error) {
                            errorMessage.value = ConsumableEvent(it.errorMessage)
                        }

                        progress.value = it.progressValue
                        progressMessage.value = it.progressMessage

                        if (it.done) {
                            progress.value = -1
                            progressMessage.value = context.getString(R.string.game_activity_message_installing)
                        }
                    }
                }
    }

    fun installGame() {
        val gameToInstall = game.value
        if (gameToInstall != null) {
            InstallGame.start(context, gameToInstall.name, gameToInstall.url, gameToInstall.title)
            viewModelScope.launch(Dispatchers.IO) {
                gamesDbHelper.saveStateToDB(gameToInstall, Game.State.IN_QUEUE_TO_INSTALL)
            }
        }
    }

    fun runGame(activityContext: Context) {
        val gameToRun = game.value
        if (gameToRun != null && gameToRun.state == Game.State.INSTALLED) {
            instead.startGame(context, gameToRun.name)
        }
    }

    fun getProgress(): LiveData<Int> = progress

    fun getGame(): LiveData<Game> = game

    fun getProgressMessage(): LiveData<String> = progressMessage

    fun getErrorMessage(): LiveData<ConsumableEvent<String>> = errorMessage

    override fun onCleared() {
        eventDisposable?.dispose()
        super.onCleared()
    }
}
