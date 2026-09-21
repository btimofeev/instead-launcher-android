/*
 * Copyright (c) 2018-2023, 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.domain.model.DownloadGameStatus.Downloading
import org.emunix.insteadlauncher.utils.resourceprovider.ResourceProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

private const val BUFFER_SIZE = 102400

const val ZIP_SIGNATURE_SIZE = 4

private val ZIP_SIGNATURES = arrayOf(
    byteArrayOf(0x50, 0x4B, 0x03, 0x04),
    byteArrayOf(0x50, 0x4B, 0x05, 0x06),
    byteArrayOf(0x50, 0x4B, 0x07, 0x08),
)

fun ByteArray.isZipArchive(): Boolean =
    ZIP_SIGNATURES.any { signature ->
        size >= signature.size && signature.indices.all { this[it] == signature[it] }
    }

@Throws(IOException::class)
suspend fun InputStream.unzip(dir: File) = coroutineScope {
    val targetDir = dir.canonicalFile
    ZipInputStream(this@unzip).use { zis ->
        while (true) {
            ensureActive()
            val entry = zis.nextEntry ?: break
            val entryFile = File(targetDir, entry.name).canonicalFile
            if (!entryFile.path.startsWith(targetDir.path + File.separator)) {
                throw ZipException("Illegal entry path: ${entry.name}")
            }
            if (entry.isDirectory) {
                if (!entryFile.isDirectory && !entryFile.mkdirs()) {
                    throw IOException("Unable to create directory: ${entryFile.path}")
                }
            } else {
                val parent = entryFile.parentFile
                if (parent != null && !parent.isDirectory && !parent.mkdirs()) {
                    throw IOException("Unable to create directory: ${parent.path}")
                }
                FileOutputStream(entryFile).use { output ->
                    val buf = ByteArray(BUFFER_SIZE)
                    while (true) {
                        ensureActive()
                        val count = zis.read(buf, 0, BUFFER_SIZE)
                        if (count == -1) break
                        output.write(buf, 0, count)
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