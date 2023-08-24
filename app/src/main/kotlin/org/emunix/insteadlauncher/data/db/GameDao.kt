/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    fun observeAll(): LiveData<List<Game>>

    @Query("SELECT * FROM games")
    fun observeAllFlow(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE name LIKE :name LIMIT 1")
    fun observeByName(name: String): LiveData<Game>

    @Query("SELECT * FROM games WHERE name LIKE :name LIMIT 1")
    fun getByName(name: String): Game // TODO похоже должно возвращать nullable, пока обработано в репозитории, но потом нужно оттуда удалить try catch

    @Query("SELECT * FROM games WHERE state = 1")
    fun observeInstalledGames(): LiveData<List<Game>>

    @Query("SELECT * FROM games WHERE state = 1")
    fun getInstalledGames(): List<Game>

    @Query("SELECT * FROM games WHERE (name || title || author || description) LIKE :query")
    fun search(query: String): LiveData<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: Game)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(games: List<Game>)

    @Update
    fun update(game: Game)

    @Delete
    fun delete(game: Game)

    @Query("DELETE FROM games WHERE name = :name")
    fun deleteByName(name: String)

    @Query("DELETE FROM games")
    fun deleteAll()

    @Transaction
    fun updateRepository(games: List<Game>) {
        getInstalledGames().forEach { installedGame ->
            for ((i, game) in games.withIndex()) {
                if (game.name == installedGame.name) {
                    games[i].installedVersion = installedGame.installedVersion
                    games[i].state = installedGame.state
                }
            }
        }

        deleteAll()
        insertAll(games)
    }
}
