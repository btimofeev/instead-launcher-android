package org.emunix.insteadlauncher.data

import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.TypeConverters

@Database(entities = arrayOf(Game::class), version = 1)
@TypeConverters(GameStateConverter::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
