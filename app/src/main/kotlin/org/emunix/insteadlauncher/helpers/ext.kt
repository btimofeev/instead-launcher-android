/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.DividerItemDecoration
import com.squareup.picasso.Picasso
import org.apache.commons.io.IOUtils
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipFile
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.zip.ZipException
import java.util.zip.ZipInputStream


fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun ImageView.loadUrl(url: String) {
    if (url.isEmpty())
        return
    Picasso.get()
            .load(url)
            .placeholder(R.drawable.walking_cat)
            .error(R.drawable.sleeping_cat)
            .into(this)
}

fun View.visible(visible: Boolean) {
    if (visible)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}

fun Context.showToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, length).show()
}

fun DividerItemDecoration.insetDivider(c: Context, @DimenRes start_offset_dimension: Int): Drawable {
    val a = c.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
    val divider = a.getDrawable(0)
    a.recycle()
    val inset = c.resources.getDimensionPixelSize(start_offset_dimension)
    val isLeftToRight = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR

    return if (isLeftToRight) {
        InsetDrawable(divider, inset, 0, 0, 0)
    } else {
        InsetDrawable(divider, 0, 0, inset, 0)
    }
}

@Throws(ZipException::class)
fun File.unzip(dir: File) {
    ZipFile(this)
            .use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryFile = File(dir, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile.mkdirs()
                        FileOutputStream(entryFile).use {
                            IOUtils.copy(zip.getInputStream(entry), it)
                        }
                    }
                }
            }
}

private const val BUFFER_SIZE = 102400

@Throws(ZipException::class)
fun InputStream.unzip(dir: File) {
    ZipInputStream(this).use { zis ->
        while (true) {
            val entry = zis.nextEntry ?: break
            val entryFile = File(dir, entry.name)
            if (entry.isDirectory) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile.mkdirs()
                FileOutputStream(entryFile).use { output ->
                    val buf = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val count = zis.read(buf, 0, BUFFER_SIZE)
                        if (count == -1) break
                        val buffer = ByteArrayInputStream(buf, 0, count)
                        IOUtils.copy(buffer, output)
                    }
                }
            }
            zis.closeEntry()
        }
    }
}

//todo переписать когда-нибудь
fun String.unescapeHtmlCodes(): String {
    var s = this.replace("&lt;", "<")
    s = s.replace("&gt;", ">")
    s = s.replace("&#039;", "\'")
    s = s.replace("&quot;", "\"")
    s = s.replace("&amp;", "&")
    return s
}

fun String.getBrief(): String {
    var s = this.take(300)
    s = s.replace("\n", " ").replace("\r", " ") // remove newlines
    s = s.replace("\\s+".toRegex(), " ") // remove double spaces
    s = s.trim()
    return s
}

fun Game.saveStateToDB(state: Game.State) {
    this.state = state
    InsteadLauncher.db.games().update(this)
}

fun Game.saveInstalledVersionToDB(version: String) {
    this.installedVersion = version
    InsteadLauncher.db.games().update(this)
}
