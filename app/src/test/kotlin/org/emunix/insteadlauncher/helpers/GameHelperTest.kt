/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

class GameHelperTest {

    @Test
    fun `is instead game`() {
        val gamePath = File(this.javaClass.getResource("/testgame")!!.path)
        assertTrue(GameHelper().isInsteadGame(gamePath))
    }

    @Test
    fun `is instead game in zip`() {
        val gamePath = File(this.javaClass.getResource("/testgame.zip")!!.path)
        assertTrue(GameHelper().isInsteadGameZip(gamePath.inputStream()))
    }

    @Test
    fun `parse game tags`() {
        val gamePath = File(this.javaClass.getResource("/testgame")!!.path)
        val gameFile = GameHelper().getMainGameFile(gamePath)
        assertEquals(gameFile.absolutePath, File(gamePath, "main3.lua").absolutePath)

        assertEquals(GameHelper().getTitle(gameFile, "ru"), "Тестовая игра")
        assertEquals(GameHelper().getAuthor(gameFile, "en"), "Test Author")
        assertEquals(GameHelper().getVersion(gameFile, "en"), "1.0")
        assertEquals(GameHelper().getInfo(gameFile, "en"), "Game for test")
    }
}