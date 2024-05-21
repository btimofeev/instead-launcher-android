/*
 * Copyright (c) 2024 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.provider

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Point
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import org.emunix.instead.core_storage_api.data.StorageImpl
import org.emunix.insteadlauncher.R
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale

class InsteadDocumentProvider : DocumentsProvider() {

    private val rootFlags = Root.FLAG_SUPPORTS_CREATE or Root.FLAG_SUPPORTS_IS_CHILD

    override fun onCreate(): Boolean = true

    @Throws(FileNotFoundException::class)
    override fun queryRoots(projection: Array<out String>?): Cursor {
        context?.let { ctx ->
            val baseDir = StorageImpl(ctx).getAppFilesDirectory()
            val appName = ctx.getString(R.string.app_name)
            return MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION).apply {
                newRow().apply {
                    add(Root.COLUMN_ROOT_ID, getDocumentId(baseDir))
                    add(Root.COLUMN_DOCUMENT_ID, getDocumentId(baseDir))
                    add(Root.COLUMN_TITLE, appName)
                    add(Root.COLUMN_SUMMARY, null)
                    add(Root.COLUMN_FLAGS, rootFlags)
                    add(Root.COLUMN_MIME_TYPES, ALL_MIME_TYPES)
                    add(Root.COLUMN_AVAILABLE_BYTES, baseDir.getFreeSpace())
                    add(Root.COLUMN_ICON, R.mipmap.ic_launcher)
                }
            }
        } ?: throw FileNotFoundException()
    }

    @Throws(FileNotFoundException::class)
    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        return MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION).apply {
            includeFile(this, getFile(documentId))
        }
    }

    @Throws(FileNotFoundException::class)
    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION).apply {
            val parent = getFile(parentDocumentId)
            parent.listFiles()?.forEach { file ->
                includeFile(this, file)
            }
        }
    }

    @Throws(FileNotFoundException::class)
    override fun openDocument(documentId: String?, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor {
        val file = getFile(documentId)
        val accessMode = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, accessMode);
    }

    @Throws(FileNotFoundException::class)
    override fun openDocumentThumbnail(
        documentId: String?,
        sizeHint: Point?,
        signal: CancellationSignal?
    ): AssetFileDescriptor {
        val file = getFile(documentId)
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        return AssetFileDescriptor(pfd, 0, file.length())
    }

    @Throws(FileNotFoundException::class)
    override fun createDocument(parentDocumentId: String?, mimeType: String, displayName: String): String {
        if (parentDocumentId == null) throw FileNotFoundException("parentDocumentId is null")
        val newFile = File(parentDocumentId, displayName)
        try {
            val isCreated: Boolean = if (Document.MIME_TYPE_DIR == mimeType) {
                newFile.mkdir()
            } else {
                newFile.createNewFile()
                newFile.setWritable(true)
                newFile.setReadable(true)
            }
            if (!isCreated) {
                throw IOException("File already exist")
            }
        } catch (e: IOException) {
            throw FileNotFoundException("Failed to create document with id " + newFile.path)
        }
        return newFile.path
    }

    @Throws(FileNotFoundException::class)
    override fun deleteDocument(documentId: String) {
        val file = getFile(documentId)
        if (!file.delete()) {
            throw FileNotFoundException("Failed to delete document $documentId")
        }
    }

    @Throws(FileNotFoundException::class)
    override fun renameDocument(documentId: String?, displayName: String?): String {
        if (displayName == null) throw FileNotFoundException("displayName is empty")
        val file = getFile(documentId)
        val dest = File(file.parent, displayName)
        if (dest.exists()) throw FileNotFoundException("Destination file already exist")
        try {
            val isRenamed = file.renameTo(dest)
            if (!isRenamed) throw IOException("Unable to rename")
        } catch (e: Exception) {
            throw FileNotFoundException("Unable to rename file $documentId to $displayName")
        }
        return getDocumentId(dest)
    }

    @Throws(FileNotFoundException::class)
    override fun getDocumentType(documentId: String?): String {
        val file: File = getFile(documentId)
        return getMimeType(file)
    }

    @Throws(FileNotFoundException::class)
    override fun isChildDocument(parentDocumentId: String?, documentId: String): Boolean {
        if (parentDocumentId == null) throw FileNotFoundException("parentDocumentId is null")
        return documentId.startsWith(parentDocumentId)
    }

    private fun getDocumentId(file: File): String = file.absolutePath

    @Throws(FileNotFoundException::class)
    private fun getFile(documentId: String?): File {
        if (documentId == null) throw FileNotFoundException("documentId is null")
        val file = File(documentId)
        if (!file.exists()) throw FileNotFoundException("File not found: ${file.absolutePath}")
        return file
    }

    @Throws(FileNotFoundException::class)
    private fun includeFile(cursor: MatrixCursor, file: File) {
        val mimeType: String = getMimeType(file)
        val flags = getFlags(file, mimeType)
        cursor.newRow().apply {
            add(Document.COLUMN_DOCUMENT_ID, getDocumentId(file))
            add(Document.COLUMN_DISPLAY_NAME, file.getName())
            add(Document.COLUMN_SIZE, file.length())
            add(Document.COLUMN_MIME_TYPE, mimeType)
            add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
            add(Document.COLUMN_FLAGS, flags)
            add(Document.COLUMN_ICON, R.mipmap.ic_launcher)
        }
    }

    private fun getFlags(file: File, mimeType: String): Int {
        var flags = when {
            file.isWritableDir() -> Document.FLAG_DIR_SUPPORTS_CREATE
            file.isWritableFile() -> Document.FLAG_SUPPORTS_WRITE
            else -> 0
        }

        file.parentFile?.let { parent ->
            if (parent.canWrite()) {
                flags = flags or Document.FLAG_SUPPORTS_DELETE or Document.FLAG_SUPPORTS_RENAME
            }
        }

        if (mimeType.startsWith("image/")) {
            flags = flags or Document.FLAG_SUPPORTS_THUMBNAIL
        }

        return flags
    }

    private fun File.isWritableDir() = isDirectory() && canWrite()

    private fun File.isWritableFile() = isFile && canWrite()

    private fun getMimeType(file: File): String {
        return if (file.isDirectory()) {
            Document.MIME_TYPE_DIR
        } else {
            val name = file.getName()
            val lastDot = name.lastIndexOf('.')
            if (lastDot >= 0) {
                val extension = name.substring(lastDot + 1).lowercase(Locale.getDefault())
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                if (mime != null) return mime
            }
            "application/octet-stream"
        }
    }

    private companion object {

        private const val ALL_MIME_TYPES = "*/*"

        private val DEFAULT_ROOT_PROJECTION = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
        )

        private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
        )
    }
}