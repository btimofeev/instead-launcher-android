/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

class GameParserTest {

    private val gameParser = GameParser()

    @Test
    fun `is instead game`() {
        val gamePath = File(this.javaClass.getResource("/testgame")!!.path)
        assertTrue(gameParser.isInsteadGame(gamePath))
    }

    @Test
    fun `is instead game in zip`() {
        val gamePath = File(this.javaClass.getResource("/testgame.zip")!!.path)
        assertTrue(gameParser.isInsteadGameZip(gamePath.inputStream()))
    }

    @Test
    fun `parse game tags`() {
        val gamePath = File(this.javaClass.getResource("/testgame")!!.path)
        val gameFile = gameParser.getMainGameFile(gamePath)
        assertEquals(gameFile.absolutePath, File(gamePath, "main3.lua").absolutePath)

        assertEquals(gameParser.getTitle(gameFile, "ru"), "Тестовая игра")
        assertEquals(gameParser.getAuthor(gameFile, "en"), "Test Author")
        assertEquals(gameParser.getVersion(gameFile, "en"), "1.0")
        assertEquals(gameParser.getInfo(gameFile, "en"), "Game for test")
    }
}