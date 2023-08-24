/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.network

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import org.emunix.insteadlauncher.data.mapper.DownloadProgress
import java.io.IOException

class ProgressResponseBody constructor(
    private val responseBody: ResponseBody,
    private val progressListener: (DownloadProgress) -> Unit,
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            var updateProgressLastTime = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                val time = System.currentTimeMillis()
                if (bytesRead == -1L || time - updateProgressLastTime >= 500) {
                    updateProgressLastTime = time
                    progressListener.invoke(
                        DownloadProgress(
                            bytesRead = totalBytesRead,
                            contentLength = responseBody.contentLength(),
                            isDone = bytesRead == -1L
                        )
                    )
                }
                return bytesRead
            }
        }
    }
}
