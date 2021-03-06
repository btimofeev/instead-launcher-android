/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data

import androidx.room.TypeConverter

class GameStateConverter {
    @TypeConverter
    fun toState(ordinal: Int): Game.State = Game.State.values()[ordinal]

    @TypeConverter
    fun toOrdinal(state: Game.State): Int = state.ordinal
}
