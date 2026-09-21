/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.emunix.insteadlauncher.domain.model.GameInfo
import org.emunix.insteadlauncher.domain.model.GameModel
import org.emunix.insteadlauncher.domain.model.GameState
import org.emunix.insteadlauncher.domain.model.GameState.INSTALLED
import org.emunix.insteadlauncher.domain.model.GameState.IS_INSTALL
import org.emunix.insteadlauncher.domain.model.GameState.NO_INSTALLED
import org.emunix.insteadlauncher.domain.model.GameUrl
import org.emunix.insteadlauncher.domain.model.GameVersion
import org.emunix.insteadlauncher.domain.model.InstallGameResult
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.DOWNLOAD_ERROR
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.GAME_NOT_FOUND_IN_DATABASE
import org.emunix.insteadlauncher.domain.model.InstallGameResult.Error.Type.UNPACKING_ERROR
import org.emunix.insteadlauncher.domain.repository.DataBaseRepository
import org.emunix.insteadlauncher.domain.repository.FileSystemRepository
import org.emunix.insteadlauncher.domain.repository.RemoteRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class InstallGameUseCaseTest {

    private lateinit var dataBaseRepository: DataBaseRepository
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var fileSystemRepository: FileSystemRepository
    private lateinit var useCase: InstallGameUseCase

    private val updates = mutableListOf<GameModel>()

    @BeforeEach
    fun setUp() {
        dataBaseRepository = mockk()
        remoteRepository = mockk()
        fileSystemRepository = mockk()
        updates.clear()
        coEvery { dataBaseRepository.updateGame(capture(updates)) } just runs
        coEvery { fileSystemRepository.deleteGameFromDisk(any()) } just runs
        useCase = InstallGameUseCaseImpl(dataBaseRepository, remoteRepository, fileSystemRepository)
    }

    @Test
    fun `returns game not found error when the game is absent in the database`() = runTest {
        coEvery { dataBaseRepository.getGame("game1") } returns null

        val result = useCase("game1", NO_INSTALLED)

        assertEquals(InstallGameResult.Error(type = GAME_NOT_FOUND_IN_DATABASE), result)
        coVerify(exactly = 0) { remoteRepository.download(any(), any()) }
        coVerify(exactly = 0) { dataBaseRepository.updateGame(any()) }
    }

    @Test
    fun `restores original state when the download fails`() = runTest {
        coEvery { dataBaseRepository.getGame("game1") } returns game()
        coEvery { remoteRepository.download(any(), "game1") } throws IOException("network error")

        val result = useCase("game1", NO_INSTALLED)

        val error = assertInstanceOf(InstallGameResult.Error::class.java, result)
        assertEquals(DOWNLOAD_ERROR, error.type)
        assertEquals(listOf(IS_INSTALL, NO_INSTALLED), updates.map { it.state })
        coVerify(exactly = 0) { fileSystemRepository.installGame(any(), any()) }
    }

    @Test
    fun `deletes partial install and marks game not installed when unpacking fails`() = runTest {
        coEvery { dataBaseRepository.getGame("game1") } returns game()
        coEvery { remoteRepository.download(any(), "game1") } returns emptyStream()
        coEvery { fileSystemRepository.installGame("game1", any()) } throws IOException("unpack error")

        val result = useCase("game1", NO_INSTALLED)

        val error = assertInstanceOf(InstallGameResult.Error::class.java, result)
        assertEquals(UNPACKING_ERROR, error.type)
        coVerify(exactly = 1) { fileSystemRepository.deleteGameFromDisk("game1") }
        assertEquals(listOf(IS_INSTALL, NO_INSTALLED), updates.map { it.state })
    }

    @Test
    fun `marks game installed with the available version on success`() = runTest {
        coEvery { dataBaseRepository.getGame("game1") } returns game()
        coEvery { remoteRepository.download(any(), "game1") } returns emptyStream()
        coEvery { fileSystemRepository.installGame("game1", any()) } just runs

        val result = useCase("game1", NO_INSTALLED)

        assertEquals(InstallGameResult.Success, result)
        val finalGame = updates.last()
        assertEquals(INSTALLED, finalGame.state)
        assertEquals("2.0", finalGame.version.installed)
        coVerify(exactly = 0) { fileSystemRepository.deleteGameFromDisk(any()) }
    }

    @Test
    fun `restores original state and rethrows when the download is cancelled`() = runTest {
        coEvery { dataBaseRepository.getGame("game1") } returns game()
        coEvery { remoteRepository.download(any(), "game1") } throws CancellationException()

        expectCancellation { useCase("game1", NO_INSTALLED) }

        assertEquals(listOf(IS_INSTALL, NO_INSTALLED), updates.map { it.state })
        coVerify(exactly = 0) { fileSystemRepository.installGame(any(), any()) }
        coVerify(exactly = 0) { fileSystemRepository.deleteGameFromDisk(any()) }
    }

    @Test
    fun `deletes partial install and marks game not installed when the install is cancelled`() = runTest {
        coEvery { dataBaseRepository.getGame("game1") } returns game()
        coEvery { remoteRepository.download(any(), "game1") } returns emptyStream()
        coEvery { fileSystemRepository.installGame("game1", any()) } throws CancellationException()

        expectCancellation { useCase("game1", NO_INSTALLED) }

        coVerify(exactly = 1) { fileSystemRepository.deleteGameFromDisk("game1") }
        assertEquals(listOf(IS_INSTALL, NO_INSTALLED), updates.map { it.state })
    }

    private fun game(name: String = "game1") = GameModel(
        name = name,
        info = GameInfo(title = name),
        url = GameUrl(download = "https://example.com/$name.zip"),
        version = GameVersion(installed = "", availableOnSite = "2.0"),
        state = NO_INSTALLED,
    )

    private fun emptyStream() = ByteArrayInputStream(ByteArray(0))

    private suspend fun expectCancellation(block: suspend () -> Unit) {
        val thrown = try {
            block()
            false
        } catch (e: CancellationException) {
            true
        }
        assertTrue(thrown, "CancellationException expected")
    }
}