/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

class UnzipTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `extracts nested entries preserving the directory structure`() = runTest {
        val dir = File(tempDir.toFile(), "out")
        val zip = zip("game/main.lua" to "print(1)", "game/sub/data.txt" to "data")

        ByteArrayInputStream(zip).unzip(dir)

        assertEquals("print(1)", File(dir, "game/main.lua").readText())
        assertEquals("data", File(dir, "game/sub/data.txt").readText())
    }

    @Test
    fun `extracts flat entries into the target directory`() = runTest {
        val dir = File(tempDir.toFile(), "out")
        val zip = zip("main.lua" to "flat", "readme.txt" to "readme")

        ByteArrayInputStream(zip).unzip(dir)

        assertEquals("flat", File(dir, "main.lua").readText())
        assertEquals("readme", File(dir, "readme.txt").readText())
    }

    @Test
    fun `creates empty directories from directory entries`() = runTest {
        val dir = File(tempDir.toFile(), "out")
        val zip = zipWithDirEntry("game/empty/")

        ByteArrayInputStream(zip).unzip(dir)

        assertTrue(File(dir, "game/empty").isDirectory)
    }

    @Test
    fun `extracts empty archive without files`() = runTest {
        val dir = File(tempDir.toFile(), "out")

        ByteArrayInputStream(zip()).unzip(dir)

        assertTrue(dir.listFiles()?.isEmpty() ?: true)
    }

    @Test
    fun `throws ZipException for a corrupted archive`() = runTest {
        val dir = File(tempDir.toFile(), "out")
        val corrupted = zip("a.lua" to "hello").also {
            it[8] = 0xFF.toByte()
            it[9] = 0xFF.toByte()
        }

        val thrown = try {
            ByteArrayInputStream(corrupted).unzip(dir)
            null
        } catch (e: Exception) {
            e
        }

        assertInstanceOf(ZipException::class.java, thrown)
    }

    @Test
    fun `rejects entries that escape the target directory`() = runTest {
        val parent = tempDir.toFile()
        val dir = File(parent, "out")
        val zip = zip("../evil.lua" to "evil")

        val thrown = try {
            ByteArrayInputStream(zip).unzip(dir)
            null
        } catch (e: Exception) {
            e
        }

        assertInstanceOf(ZipException::class.java, thrown)
        assertFalse(File(parent, "evil.lua").exists())
    }

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zos ->
            entries.forEach { (name, content) ->
                zos.putNextEntry(ZipEntry(name))
                zos.write(content.toByteArray())
                zos.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun zipWithDirEntry(name: String): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zos ->
            zos.putNextEntry(ZipEntry(name))
            zos.closeEntry()
        }
        return output.toByteArray()
    }
}