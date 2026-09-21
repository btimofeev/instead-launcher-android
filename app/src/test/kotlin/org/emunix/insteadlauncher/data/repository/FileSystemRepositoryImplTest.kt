/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.repository

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.emunix.instead.core_storage_api.data.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileSystemRepositoryImplTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var appFilesDir: File
    private lateinit var gamesDir: File
    private lateinit var repository: FileSystemRepositoryImpl

    @BeforeEach
    fun setUp() {
        appFilesDir = File(tempDir.toFile(), "files")
        gamesDir = File(appFilesDir, "games").apply { mkdirs() }
        val storage = mockk<Storage> {
            every { getAppFilesDirectory() } returns appFilesDir
            every { getGamesDirectory() } returns gamesDir
        }
        repository = FileSystemRepositoryImpl(storage)
    }

    @Test
    fun `install game unpacks zip into games dir and removes temp files`() = runTest {
        val zipBytes = createZip("game1/main.lua" to "print('hello')", "game1/readme.txt" to "readme")

        repository.installGame("game1", ByteArrayInputStream(zipBytes))

        val mainFile = File(gamesDir, "game1/main.lua")
        assertTrue(mainFile.exists())
        assertEquals("print('hello')", mainFile.readText())
        assertTrue(File(gamesDir, "game1/readme.txt").exists())
        assertFalse(File(appFilesDir, "game1.zip").exists())
        assertFalse(File(appFilesDir, ".tmp-game1").exists())
    }

    @Test
    fun `install game replaces the existing version on disk`() = runTest {
        createGameDirWithFile("game1", "main.lua", "old version")
        val zipBytes = createZip("game1/main.lua" to "new version", "game1/new_file.txt" to "added")

        repository.installGame("game1", ByteArrayInputStream(zipBytes))

        val gameDir = File(gamesDir, "game1")
        assertEquals("new version", File(gameDir, "main.lua").readText())
        assertTrue(File(gameDir, "new_file.txt").exists())
    }

    @Test
    fun `install game with flat zip content`() = runTest {
        val zipBytes = createZip("a.lua" to "one", "sub/b.txt" to "two")

        repository.installGame("game1", ByteArrayInputStream(zipBytes))

        val gameDir = File(gamesDir, "game1")
        assertEquals("one", File(gameDir, "a.lua").readText())
        assertEquals("two", File(gameDir, "sub/b.txt").readText())
        assertFalse(File(appFilesDir, "game1.zip").exists())
        assertFalse(File(appFilesDir, ".tmp-game1").exists())
    }

    @Test
    fun `cleanupGameTempFiles removes leftover temp files and keeps the game dir`() = runTest {
        File(appFilesDir, "game1.zip").writeText("partial")
        File(File(appFilesDir, ".tmp-game1"), "main.lua").apply { parentFile?.mkdirs() }
            .writeText("partial")
        createGameDirWithFile("game1", "main.lua", "installed")

        repository.cleanupGameTempFiles("game1")

        assertFalse(File(appFilesDir, "game1.zip").exists())
        assertFalse(File(appFilesDir, ".tmp-game1").exists())
        assertTrue(File(gamesDir, "game1/main.lua").exists())
    }

    @Test
    fun `deleteGameFromDisk removes the game dir and temp leftovers`() = runTest {
        createGameDirWithFile("game2", "main.lua", "installed")
        File(appFilesDir, "game2.zip").writeText("partial")
        File(File(appFilesDir, ".tmp-game2"), "main.lua").apply { parentFile?.mkdirs() }
            .writeText("partial")

        repository.deleteGameFromDisk("game2")

        assertFalse(File(gamesDir, "game2").exists())
        assertFalse(File(appFilesDir, "game2.zip").exists())
        assertFalse(File(appFilesDir, ".tmp-game2").exists())
    }

    private fun createZip(vararg entries: Pair<String, String>): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ZipOutputStream(byteArrayOutputStream).use { zip ->
            entries.forEach { (path, content) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return byteArrayOutputStream.toByteArray()
    }

    private fun createGameDirWithFile(gameName: String, file: String, content: String) {
        val fileInGameDir = File(File(gamesDir, gameName), file)
        fileInGameDir.parentFile?.mkdirs()
        fileInGameDir.writeText(content)
    }
}