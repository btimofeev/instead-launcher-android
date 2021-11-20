/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead

import android.content.Context
import android.content.Intent
import org.emunix.instead.ui.InsteadActivity
import org.emunix.instead_api.InsteadApi
import javax.inject.Inject

class InsteadApiImpl @Inject constructor(val context: Context) : InsteadApi {

    override fun startGame(gameName: String, playFromBeginning: Boolean) {
        val intent = Intent(context, InsteadActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("game_name", gameName)
        intent.putExtra("play_from_beginning", playFromBeginning)
        context.startActivity(intent)
    }
}