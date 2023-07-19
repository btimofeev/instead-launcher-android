/*
 * Copyright (c) 2018-2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.DividerItemDecoration
import coil.load
import coil.size.Precision.EXACT
import org.apache.commons.io.IOUtils
import org.emunix.insteadlauncher.R
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun ImageView.loadUrl(url: String, highQuality: Boolean = false) {
    load(url.ifEmpty { R.drawable.sleeping_cat }) {
        placeholder(R.drawable.walking_cat)
        error(R.drawable.sleeping_cat)
        if (highQuality) {
            precision(EXACT)
        }
    }
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
                        entryFile.parentFile?.mkdirs()
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
                entryFile.parentFile?.mkdirs()
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

// https://gist.github.com/kevinmcmahon/2988931
@Suppress("DEPRECATION") // Deprecated for third party Services.
fun <T> Context.isServiceRunning(service: Class<T>) =
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == service.name }