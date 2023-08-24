/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.installedgames

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.models.InstalledGame
import org.emunix.insteadlauncher.utils.loadUrl

class InstalledGamesAdapter(
    val onClickListener: (gameName: String) -> Unit
) : ListAdapter<InstalledGame, InstalledGamesAdapter.ViewHolder>(DiffCallback()) {

    val longClickedGameName
        get() = longClickedGame?.name

    private var longClickedGame: InstalledGame? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView? = itemView.findViewById(R.id.name)
        val image : ImageView? = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_installed_games, parent, false))
        holder.itemView.setOnClickListener { onClickListener(getItem(holder.bindingAdapterPosition).name) }
        holder.itemView.setOnLongClickListener {
            longClickedGame = getItem(holder.bindingAdapterPosition)
            false
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = getItem(position)
        holder.name?.text = game.title
        holder.image?.loadUrl(game.imageUrl)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).name.hashCode().toLong()
    }

    class DiffCallback : DiffUtil.ItemCallback<InstalledGame>() {

        override fun areItemsTheSame(oldItem: InstalledGame, newItem: InstalledGame): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: InstalledGame, newItem: InstalledGame): Boolean {
            return oldItem == newItem
        }
    }
}
