package org.emunix.insteadlauncher.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game

class GameViewModel : ViewModel() {
    private lateinit var game: LiveData<Game>

    fun init(gameName: String) {
        game = InsteadLauncher.gamesDB.gameDao().getByName(gameName)
    }

    fun getGame(): LiveData<Game> = game
}
