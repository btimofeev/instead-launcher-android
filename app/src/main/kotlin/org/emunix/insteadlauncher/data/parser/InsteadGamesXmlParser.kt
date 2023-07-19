/*
 * Copyright (c) 2018, 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.parser

import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.Game.State.NO_INSTALLED
import org.emunix.insteadlauncher.helpers.getBrief
import org.emunix.insteadlauncher.helpers.unescapeHtmlCodes
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class InsteadGamesXmlParser : GameListParser {

    override fun parse(input: String): Map<String, Game> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(input))
        parser.nextTag()
        return readFeed(parser)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): Map<String, Game> {
        val games = mutableMapOf<String, Game>()

        parser.require(XmlPullParser.START_TAG, null, "game_list")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "game") {
                val entry = readEntry(parser)
                games[entry.name] = entry
            } else {
                skip(parser)
            }
        }
        return games
    }

    private fun readEntry(parser: XmlPullParser): Game {
        var gName = ""
        var gTitle = ""
        var gAuthor = ""
        var gImage = ""
        var gDate = ""
        var gVersion = ""
        var gUrl = ""
        var gSize = 0L
        var gLang = ""
        var gDescription = ""
        var gDescUrl = ""

        parser.require(XmlPullParser.START_TAG, null, "game")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (val name = parser.name) {
                "name" -> gName = readTag(name, parser)
                "title" -> gTitle = readTag(name, parser)
                "author" -> gAuthor = readTag(name, parser)
                "image" -> gImage = readTag(name, parser)
                "date" -> gDate = readTag(name, parser)
                "version" -> gVersion = readTag(name, parser)
                "url" -> gUrl = readTag(name, parser)
                "size" -> gSize = readTag(name, parser).toLong()
                "lang" -> gLang = readTag(name, parser)
                "description" -> gDescription = readTag(name, parser)
                "descurl" -> gDescUrl = readTag(name, parser)
                else -> skip(parser)
            }
        }
        gTitle = gTitle.unescapeHtmlCodes()
        gDescription = gDescription.unescapeHtmlCodes()
        gAuthor = gAuthor.unescapeHtmlCodes()
        val gBrief: String = gDescription.getBrief()
        if (!gImage.contains(".png", true) && !gImage.contains(".jpg", true)) {
            gImage = ""
        }
        return Game(
            name = gName,
            title = gTitle,
            author = gAuthor,
            date = gDate,
            version = gVersion,
            size = gSize,
            url = gUrl,
            image = gImage,
            lang = gLang,
            description = gDescription,
            descurl = gDescUrl,
            brief = gBrief,
            installedVersion = "",
            state = NO_INSTALLED
        )
    }

    private fun readTag(tag: String, parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, tag)
        return result
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}