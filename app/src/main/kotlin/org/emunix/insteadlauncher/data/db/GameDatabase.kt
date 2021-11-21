/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Game::class), version = 1)
@TypeConverters(GameStateConverter::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun games(): GameDao
}
