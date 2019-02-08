package org.emunix.insteadlauncher.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import org.apache.commons.io.IOUtils
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.data.Game
import java.io.File
import java.util.zip.ZipFile
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
