/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.manager.game.GameManager
import javax.inject.Inject

@HiltViewModel
class TvControlsHintViewModel @Inject constructor(
    private val preferencesProvider: PreferencesProvider,
    private val gameManager: GameManager,
) : ViewModel() {

    fun shouldShowGameControlsHint(): Boolean = preferencesProvider.isGameControlsHintEnabled

    fun closeAndStartGame(gameName: String) {
        gameManager.startGame(gameName)
    }

    fun disableAndStartGame(gameName: String) {
        preferencesProvider.isGameControlsHintEnabled = false
        gameManager.startGame(gameName)
    }
}
