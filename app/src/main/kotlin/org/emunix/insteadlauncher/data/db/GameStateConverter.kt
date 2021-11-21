/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data.db

import androidx.room.TypeConverter
import org.emunix.insteadlauncher.data.db.Game.State

class GameStateConverter {
    @TypeConverter
    fun toState(ordinal: Int): State = State.values()[ordinal]

    @TypeConverter
    fun toOrdinal(state: State): Int = state.ordinal
}
