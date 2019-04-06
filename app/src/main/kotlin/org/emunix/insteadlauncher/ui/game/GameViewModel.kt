/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.game

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.event.DownloadProgressEvent
import org.emunix.insteadlauncher.helpers.RxBus
import org.emunix.insteadlauncher.helpers.showToast
import org.emunix.insteadlauncher.services.InstallGame

class GameViewModel(var app: Application) : AndroidViewModel(app) {
    private lateinit var game: LiveData<Game>
    private var progress: MutableLiveData<Int> = MutableLiveData()
    private var progressMessage: MutableLiveData<String> = MutableLiveData()

    @SuppressLint("CheckResult")
    fun init(gameName: String) {
        game = InsteadLauncher.db.games().observeByName(gameName)

        RxBus.listen(DownloadProgressEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.gameName == gameName) {
                        if (it.error) {
                            app.showToast(it.errorMessage)
                        }

                        progress.value = it.progressValue
                        progressMessage.value = it.progressMessage

                        if (it.done) {
                            progress.value = -1
                            progressMessage.value = app.getString(R.string.game_activity_message_installing)
                        }
                    }
                }
    }

    fun getProgress(): LiveData<Int> = progress

    fun getGame(): LiveData<Game> = game

    fun getProgressMessage(): LiveData<String> = progressMessage
}
