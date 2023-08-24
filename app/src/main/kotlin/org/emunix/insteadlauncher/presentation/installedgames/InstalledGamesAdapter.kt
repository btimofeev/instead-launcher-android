/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.utils.loadUrl
import androidx.recyclerview.widget.ListAdapter
import org.emunix.insteadlauncher.data.db.GameDiffCallback
import timber.log.Timber

class InstalledGamesAdapter(val onClickListener: (Game) -> Unit) : ListAdapter<Game, InstalledGamesAdapter.ViewHolder>(
    GameDiffCallback()
) {

    lateinit var longClickedGame: Game
    private set

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
        val image = itemView.findViewById<ImageView>(R.id.image)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_installed_games, parent, false))
        holder.itemView.setOnClickListener { onClickListener(getItem(holder.adapterPosition)) }
        holder.itemView.setOnLongClickListener {
            Timber.d("Long clicked game: ${getItem(holder.adapterPosition).name}")
            longClickedGame = getItem(holder.adapterPosition)
            false
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = getItem(position)
        holder.name.text = game.title
        holder.image.loadUrl(game.image)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).name.hashCode().toLong()
    }
}
