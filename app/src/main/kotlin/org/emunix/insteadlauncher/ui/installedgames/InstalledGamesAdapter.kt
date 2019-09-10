/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.loadUrl
import androidx.recyclerview.widget.ListAdapter
import org.emunix.insteadlauncher.data.GameDiffCallback
import org.emunix.insteadlauncher.data.GameDiffCallback.Diff

class InstalledGamesAdapter(val onClickListener: (Game) -> Unit) : ListAdapter<Game, InstalledGamesAdapter.ViewHolder>(GameDiffCallback()) {

    private lateinit var longClickedGame: Game

    fun getLongClickedGame(): Game = longClickedGame

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
        val image = itemView.findViewById<ImageView>(R.id.image)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_installed_games_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = getItem(position).title
        holder.image.loadUrl(getItem(position).image)
        holder.itemView.setOnClickListener { onClickListener(getItem(position)) }
        holder.itemView.setOnLongClickListener {
            longClickedGame = getItem(position)
            false
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val diff = payloads[0] as List<Diff>
            for (key in diff) {
                when (key) {
                    Diff.IMAGE -> holder.image.loadUrl(getItem(position).image)
                    Diff.BRIEF -> getItem(position).title
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).name.hashCode().toLong()
    }
}
