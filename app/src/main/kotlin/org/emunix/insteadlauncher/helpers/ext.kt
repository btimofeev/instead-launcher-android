package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.zip.ZipFile
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipException


fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun ImageView.loadUrl(url: String) {
    Picasso.get().load(url).into(this)
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

@Throws (ZipException::class)
fun File.unzip(dir: File) {
    val zip = ZipFile(this)
    val entries = zip.entries()
    while (entries.hasMoreElements()) {
        val entry = entries.nextElement()
        if (entry.isDirectory) {
            File(dir, entry.name).mkdirs()
        } else {
            val fos = FileOutputStream(File(dir, entry.name))
            val bos = BufferedOutputStream(fos)
            IOUtils.copy(zip.getInputStream(entry), bos)
            IOUtils.closeQuietly(bos, fos)
        }
    }
    IOUtils.closeQuietly(zip)
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
