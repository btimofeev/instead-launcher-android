/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import kotlinx.coroutines.test.runTest
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.domain.ActiveDownloadsRegistry
import org.emunix.insteadlauncher.domain.model.GameInfo
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.GameUrl
import org.emunix.insteadlauncher.domain.model.GameVersion
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class RecoverInterruptedOperationsUseCaseTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var gamesDir: File
    private lateinit var dataBaseRepository: DataBaseRepository
    private lateinit var fileSystemRepository: FileSystemRepository
    private lateinit var activeDownloadsRegistry: ActiveDownloadsRegistry
    private lateinit var useCase: RecoverInterruptedOperationsUseCase

    @BeforeEach
    fun setUp() {
        gamesDir = File(File(tempDir.toFile(), "files"), "games").apply { mkdirs() }
        dataBaseRepository = mockk()
        fileSystemRepository = mockk()
        activeDownloadsRegistry = mockk { every { activeGameNames() } returns emptySet() }
        val storage = mockk<Storage> {
            every { getGamesDirectory() } returns gamesDir
        }
        useCase = RecoverInterruptedOperationsUseCaseImpl(
            dataBaseRepository = dataBaseRepository,
            fileSystemRepository = fileSystemRepository,
            storage = storage,
            activeDownloadsRegistry = activeDownloadsRegistry,
        )
    }

    @Test
    fun `returns false when there are no stuck games`() = runTest {
        coEvery { dataBaseRepository.getStuckGames() } returns emptyList()

        assertFalse(useCase())

        coVerify(exactly = 1) { dataBaseRepository.getStuckGames() }
    }

    @Test
    fun `games that are being downloaded right now are skipped`() = runTest {
        val activeGame = game("game1", GameState.IS_INSTALL)
        coEvery { dataBaseRepository.getStuckGames() } returns listOf(activeGame)
        every { activeDownloadsRegistry.activeGameNames() } returns setOf("game1")

        assertFalse(useCase())

        coVerify(exactly = 0) { fileSystemRepository.cleanupGameTempFiles(any()) }
        coVerify(exactly = 0) { dataBaseRepository.updateGame(any()) }
        coVerify(exactly = 0) { dataBaseRepository.markAsNotInstalled(any()) }
    }

    @Test
    fun `stuck installation with game dir present is restored to installed`() = runTest {
        val stuckGame = game("game1", GameState.IS_INSTALL)
        coEvery { dataBaseRepository.getStuckGames() } returns listOf(stuckGame)
        coEvery { fileSystemRepository.cleanupGameTempFiles("game1") } just runs
        coEvery { dataBaseRepository.updateGame(any()) } just runs
        File(gamesDir, "game1").mkdirs()

        assertTrue(useCase())

        coVerify(exactly = 1) { fileSystemRepository.cleanupGameTempFiles("game1") }
        coVerify(exactly = 1) {
            dataBaseRepository.updateGame(match { it.name == "game1" && it.state == GameState.INSTALLED })
        }
        coVerify(exactly = 0) { dataBaseRepository.markAsNotInstalled(any()) }
    }

    @Test
    fun `stuck installation without game dir is restored to not installed`() = runTest {
        val stuckGame = game("game1", GameState.IS_INSTALL)
        coEvery { dataBaseRepository.getStuckGames() } returns listOf(stuckGame)
        coEvery { fileSystemRepository.cleanupGameTempFiles("game1") } just runs
        coEvery { dataBaseRepository.markAsNotInstalled(any()) } just runs

        assertTrue(useCase())

        coVerify(exactly = 1) { dataBaseRepository.markAsNotInstalled(match { it.name == "game1" }) }
        coVerify(exactly = 0) { dataBaseRepository.updateGame(any()) }
    }

    @Test
    fun `updates queued before download with old version on disk are kept installed`() = runTest {
        val stuckGame = game("game1", GameState.IN_QUEUE_TO_INSTALL)
        coEvery { dataBaseRepository.getStuckGames() } returns listOf(stuckGame)
        coEvery { fileSystemRepository.cleanupGameTempFiles("game1") } just runs
        coEvery { dataBaseRepository.updateGame(any()) } just runs
        File(gamesDir, "game1").mkdirs()

        assertTrue(useCase())

        coVerify(exactly = 1) {
            dataBaseRepository.updateGame(match { it.name == "game1" && it.state == GameState.INSTALLED })
        }
    }

    @Test
    fun `stuck deletion of a game from the site is finished`() = runTest {
        val stuckGame = game("game1", GameState.IS_DELETE)
        coEvery { dataBaseRepository.getStuckGames() } returns listOf(stuckGame)
        coEvery { fileSystemRepository.deleteGameFromDisk("game1") } just runs
        coEvery { dataBaseRepository.markAsNotInstalled(any()) } just runs
        File(gamesDir, "game1").mkdirs()

        assertTrue(useCase())

        coVerify(exactly = 1) { fileSystemRepository.deleteGameFromDisk("game1") }
        coVerify(exactly = 1) { dataBaseRepository.markAsNotInstalled(stuckGame) }
        coVerify(exactly = 0) { dataBaseRepository.deleteGame(any()) }
    }

    @Test
    fun `stuck deletion of a local game removes its record`() = runTest {
        val stuckGame = game("game1", GameState.IS_DELETE, site = "")
        coEvery { dataBaseRepository.getStuckGames() } returns listOf(stuckGame)
        coEvery { fileSystemRepository.deleteGameFromDisk("game1") } just runs
        coEvery { dataBaseRepository.deleteGame("game1") } just runs
        File(gamesDir, "game1").mkdirs()

        assertTrue(useCase())

        coVerify(exactly = 1) { fileSystemRepository.deleteGameFromDisk("game1") }
        coVerify(exactly = 1) { dataBaseRepository.deleteGame("game1") }
        coVerify(exactly = 0) { dataBaseRepository.markAsNotInstalled(any()) }
    }

    private fun game(name: String, state: GameState, site: String = "https://example.com") =
        GameModel(
            name = name,
            info = GameInfo(title = name),
            url = GameUrl(site = site),
            version = GameVersion(installed = "1.0", availableOnSite = "2.0"),
            state = state,
        )
}