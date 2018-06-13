package org.emunix.insteadlauncher.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "games")
data class Game (
        @PrimaryKey(autoGenerate = false)
        val name: String,
        val title: String,
        val author: String,
        val version: String,
        val size: Long,
        val url: String,
        val image: String,
        val lang: String,
        val description: String,
        val descurl: String,
        @ColumnInfo(name = "installed_version")
        var installedVersion: String,
        var state: State
) {
    enum class State {
        NO_INSTALLED,
        INSTALLED,
        IS_INSTALL,
        IS_DELETE,
        IS_UPDATE
    }
}