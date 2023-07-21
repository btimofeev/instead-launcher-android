/*
 * Copyright (c) 2019-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.gameparser

import org.emunix.insteadlauncher.domain.parser.GameParser
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import javax.inject.Inject

class GameParserImpl @Inject constructor() : GameParser {

    override fun isInsteadGame(dir: File): Boolean {
        val main3 = File(dir, "main3.lua")
        val main = File(dir, "main.lua")
        return main3.exists() or main.exists()
    }

    @Throws(ZipException::class)
    override fun isInsteadGameZip(inputStream: InputStream): Boolean {
        var r = false
        ZipInputStream(inputStream).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                val name = entry.name
                zis.closeEntry()
                if (name.contains("main3.lua") or name.contains("main.lua")) {
                    r = true
                    break
                }
            }
        }
        return r
    }

    override fun getMainGameFile(dir: File): File {
        val main3 = File(dir, "main3.lua")
        if (main3.exists())
            return main3

        val main = File(dir, "main.lua")
        if (main.exists())
            return main

        throw IllegalStateException("main*.lua not found")
    }

    override fun getTitle(file: File, locale: String): String {
        val regex = Regex("\\s*(?:--)\\s*\\\$Name(?:\\((\\w\\w)\\))?:(.*)\\\$")
        return parseFrom(file, regex, locale) ?: file.parentFile.name
    }

    override fun getAuthor(file: File, locale: String): String {
        val regex = Regex("\\s*(?:--)\\s*\\\$Author(?:\\((\\w\\w)\\))?:(.*)\\\$")
        return parseFrom(file, regex, locale) ?: ""
    }

    override fun getVersion(file: File, locale: String): String {
        val regex = Regex("\\s*(?:--)\\s*\\\$Version(?:\\((\\w\\w)\\))?:(.*)\\\$")
        return parseFrom(file, regex, locale) ?: "0"
    }

    override fun getInfo(file: File, locale: String): String {
        val regex = Regex("\\s*(?:--)\\s*\\\$Info(?:\\((\\w\\w)\\))?:(.*)\\\$")
        return parseFrom(file, regex, locale) ?: ""
    }

    private fun parseFrom(file: File, regex: Regex, locale: String): String? {
        val entries = hashMapOf<String, String?>(DEFAULT_LANG to null)
        val commentPattern = Pattern.compile("\\s*--.*")

        file.forEachLine { line ->
            if (!commentPattern.matcher(line).matches())
                return@forEachLine
            val matchResult = regex.find(line)
            if (matchResult != null) {
                val (lang, text) = matchResult.destructured
                if (lang.isBlank()) {
                    entries[DEFAULT_LANG] = text.trim()
                } else {
                    entries[lang] = text.trim()
                }
            }
        }

        return if (entries.containsKey(locale))
            entries[locale]
        else
            entries[DEFAULT_LANG]
    }

    companion object {

        private const val DEFAULT_LANG = "default_lang"
    }
}
