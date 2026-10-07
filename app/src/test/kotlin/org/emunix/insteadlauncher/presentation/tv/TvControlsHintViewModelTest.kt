/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import io.mockk.mockk
import io.mockk.verify
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.insteadlauncher.manager.game.GameManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TvControlsHintViewModelTest {

    private val preferencesProvider: PreferencesProvider = mockk(relaxed = true)
    private val gameManager: GameManager = mockk(relaxed = true)

    private lateinit var viewModel: TvControlsHintViewModel

    @BeforeEach
    fun setUp() {
        viewModel = TvControlsHintViewModel(preferencesProvider, gameManager)
    }

    @Test
    fun `closeAndStartGame starts the game and keeps the setting`() {
        viewModel.closeAndStartGame(GAME_NAME)

        verify(exactly = 1) { gameManager.startGame(GAME_NAME, false) }
        verify(exactly = 0) { preferencesProvider.isGameControlsHintEnabled = any() }
    }

    @Test
    fun `disableAndStartGame turns the setting off and starts the game`() {
        viewModel.disableAndStartGame(GAME_NAME)

        verify(exactly = 1) { preferencesProvider.isGameControlsHintEnabled = false }
        verify(exactly = 1) { gameManager.startGame(GAME_NAME, false) }
    }

    companion object {
        private const val GAME_NAME = "mytheria"
    }
}
