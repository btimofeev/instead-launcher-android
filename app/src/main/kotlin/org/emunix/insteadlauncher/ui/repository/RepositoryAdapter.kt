/*
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.GameDiffCallback
import org.emunix.insteadlauncher.data.GameDiffCallback.Diff
import org.emunix.insteadlauncher.helpers.loadUrl
import org.emunix.insteadlauncher.helpers.visible

class RepositoryAdapter(val listener: (Game, ImageView) -> Unit): ListAdapter<Game, RepositoryAdapter.ViewHolder>(GameDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
        val image = itemView.findViewById<ImageView>(R.id.image)!!
        val description = itemView.findViewById<TextView>(R.id.description)!!
        val badge = itemView.findViewById<ImageView>(R.id.badge)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_repository_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = getItem(position).title
        updateImage(holder, position)
        holder.description.text = getItem(position).brief
        holder.itemView.setOnClickListener { listener(getItem(position), holder.image) }
        updateBadge(holder, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        ViewCompat.setTransitionName(holder.image, getItem(position).name)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val diff = payloads[0] as List<Diff>
            for (key: Diff in diff) {
                when (key) {
                    Diff.IMAGE -> updateImage(holder, position)
                    Diff.TITLE -> holder.name.text = getItem(position).title
                    Diff.BRIEF -> holder.description.text = getItem(position).brief
                    Diff.VERSION -> updateBadge(holder, position)
                }
            }
        }
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
