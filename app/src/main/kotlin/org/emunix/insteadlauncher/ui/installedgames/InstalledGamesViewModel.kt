/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game

class InstalledGamesViewModel(var app: Application): AndroidViewModel(app) {

    private val games = InsteadLauncher.db.games().observeInstalledGames()

    fun getInstalledGames(): LiveData<List<Game>> = games
}
