package org.emunix.insteadlauncher.helpers

import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import org.emunix.insteadlauncher.data.Game
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class InsteadGamesXMLParser {

    fun parse(input: String): List<Game> {
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(input))
            parser.nextTag()
            return readFeed(parser)
        } finally {

        }
    }

    @Throws (XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<Game> {
        val games = ArrayList<Game>()

        parser.require(XmlPullParser.START_TAG, null, "game_list")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "game") {
                games.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return games
    }

    private fun readEntry(parser: XmlPullParser): Game {
        var gName: String = ""
        var gTitle: String = ""
        var gAuthor: String = ""
        var gImage: String = ""
        var gVersion: String = ""
        var gUrl: String = ""
        var gSize: Long = 0
        var gLang: String = ""
        var gDescription: String = ""
        var gDescurl: String = ""

        parser.require(XmlPullParser.START_TAG, null, "game")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            when(name) {
                "name"  -> gName = readTag(name, parser)
                "title" -> gTitle = readTag(name, parser)
                "author" -> gAuthor = readTag(name, parser)
                "image" -> gImage = readTag(name, parser)
                "version" -> gVersion = readTag(name, parser)
                "url" -> gUrl = readTag(name, parser)
                "size" -> gSize = readTag(name, parser).toLong()
                "lang" -> gLang = readTag(name, parser)
                "description" -> gDescription = readTag(name, parser)
                "descurl" -> gDescurl = readTag(name, parser)
                else -> skip(parser)
            }
        }
        return Game(gName, gTitle, gAuthor, gVersion, gSize, gUrl, gImage, gLang, gDescription, gDescurl, false, "")
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