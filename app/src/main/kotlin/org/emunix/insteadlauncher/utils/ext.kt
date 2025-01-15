/*
 * Copyright (c) 2018-2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.DividerItemDecoration
import coil.load
import coil.size.Precision.EXACT
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.zip.ZipException
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

fun DividerItemDecoration.insetDivider(context: Context, @DimenRes startOffsetDimension: Int): Drawable {
    val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
    val divider = a.getDrawable(0)
    a.recycle()
    val inset = context.resources.getDimensionPixelSize(startOffsetDimension)
    val isLeftToRight = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR

    return if (isLeftToRight) {
        InsetDrawable(divider, inset, 0, 0, 0)
    } else {
        InsetDrawable(divider, 0, 0, inset, 0)
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

fun Throwable.writeToLog() {
    Timber.tag("InsteadLauncher").e(this)
}

fun Downloading.getDownloadingMessage(resourceProvider: ResourceProvider): String =
    resourceProvider.getString(
        R.string.game_activity_message_downloading,
        FileUtils.byteCountToDisplaySize(downloadedBytes),
        if (contentLength == -1L) "??" else FileUtils.byteCountToDisplaySize(contentLength)
    )

fun Context.launchBrowser(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(browserIntent)
}