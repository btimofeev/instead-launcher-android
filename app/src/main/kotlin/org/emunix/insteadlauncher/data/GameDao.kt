package org.emunix.insteadlauncher.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.reactivex.Flowable

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    fun getAll(): LiveData<List<Game>>

    @Query("SELECT * FROM games WHERE name LIKE :name LIMIT 1")
    fun getByName(name: String): LiveData<Game>

    @Query("SELECT * FROM games WHERE name LIKE :name LIMIT 1")
    fun getGameByName(name: String): Game

    @Query("SELECT * FROM games WHERE state = 1")
    fun getInstalledGames(): Flowable<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: Game)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(games: List<Game>)

    @Update
    fun update(game: Game)

    @Delete
    fun delete(game: Game)

    @Query("DELETE FROM games")
    fun deleteAll()
}
