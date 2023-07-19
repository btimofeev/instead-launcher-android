/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.db.Game
import org.emunix.insteadlauncher.data.db.GameDiffCallback
import org.emunix.insteadlauncher.helpers.loadUrl
import org.emunix.insteadlauncher.helpers.visible

class RepositoryAdapter(val listener: (Game, ImageView) -> Unit): ListAdapter<Game, RepositoryAdapter.ViewHolder>(
    GameDiffCallback()
) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
        val image = itemView.findViewById<ImageView>(R.id.image)!!
        val description = itemView.findViewById<TextView>(R.id.description)!!
        val badge = itemView.findViewById<ImageView>(R.id.badge)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false))
        holder.itemView.setOnClickListener { listener(getItem(holder.adapterPosition), holder.image) }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = getItem(position)
        holder.name.text = game.title
        updateImage(holder, position)
        holder.description.text = game.brief
        updateBadge(holder, position)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).name.hashCode().toLong()
    }

    private fun updateImage(holder: ViewHolder, position: Int) {
            holder.image.loadUrl(getItem(position).image)
    }

    private fun updateBadge(holder: ViewHolder, position: Int) {
        if (getItem(position).installedVersion.isNotBlank() and (getItem(position).version != getItem(position).installedVersion)) {
            holder.badge.visible(true)
        } else {
            holder.badge.visible(false)
        }
    }
}
