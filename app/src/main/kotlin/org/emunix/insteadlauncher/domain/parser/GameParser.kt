/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.domain.parser

import java.io.File
import java.io.InputStream
import java.lang.IllegalStateException

interface GameParser {

    fun isInsteadGame(dir: File): Boolean

    fun isInsteadGameZip(inputStream: InputStream): Boolean

    @Throws(IllegalStateException::class)
    fun getMainGameFile(dir: File): File

    fun getTitle(file: File, locale: String): String

    fun getAuthor(file: File, locale: String): String

    fun getVersion(file: File, locale: String): String

    fun getInfo(file: File, locale: String): String
}