/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import org.emunix.insteadlauncher.data.GameDao
import org.emunix.insteadlauncher.data.GameDatabase
import javax.inject.Singleton

@Module
class DatabaseModule() {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): GameDatabase =
            Room.databaseBuilder(context, GameDatabase::class.java, "games.db").build()

    @Provides
    @Singleton
    fun provideGameDAO(db: GameDatabase): GameDao = db.games()
}