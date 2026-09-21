/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class IsZipArchiveTest {

    @Test
    fun `recognizes a regular zip archive`() {
        val zip = zip("main.lua" to "print(1)")

        assertTrue(zip.isZipArchive())
    }

    @Test
    fun `recognizes an empty zip archive`() {
        assertTrue(zip().isZipArchive())
    }

    @Test
    fun `recognizes only the signature prefix`() {
        assertTrue(byteArrayOf(0x50, 0x4B, 0x03, 0x04).isZipArchive())
    }

    @Test
    fun `rejects a plain text response`() {
        assertFalse("File not found: instead-ilines-1.2.zip".toByteArray().isZipArchive())
    }

    @Test
    fun `rejects a response shorter than the signature`() {
        assertFalse(byteArrayOf(0x50, 0x4B).isZipArchive())
        assertFalse(ByteArray(0).isZipArchive())
    }

    @Test
    fun `rejects a response starting with PK but not a zip signature`() {
        assertFalse("PK something else".toByteArray().isZipArchive())
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
}
