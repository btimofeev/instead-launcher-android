package org.emunix.insteadlauncher.data

import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database

@Database(entities = arrayOf(Game::class), version = 1)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
