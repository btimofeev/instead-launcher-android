/*
 * Copyright (c) 2018-2021, 2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.emunix.insteadlauncher.databinding.ItemRepositoryBinding
import org.emunix.insteadlauncher.presentation.models.RepoGame
import org.emunix.insteadlauncher.utils.loadUrl
import org.emunix.insteadlauncher.utils.visible

class RepositoryAdapter(
    val listener: (RepoGame, ImageView) -> Unit
): ListAdapter<RepoGame, RepositoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemRepositoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRepositoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding).also { holder ->
            holder.itemView.setOnClickListener { listener(getItem(holder.bindingAdapterPosition), binding.image) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = getItem(position)
        with(holder.binding) {
            name.text = game.title
            description.text = game.description
            image.loadUrl(game.imageUrl)
            badge.visible(game.isHasNewVersion)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).name.hashCode().toLong()
    }

    private class DiffCallback : DiffUtil.ItemCallback<RepoGame>() {

        override fun areItemsTheSame(oldItem: RepoGame, newItem: RepoGame): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RepoGame, newItem: RepoGame): Boolean {
            return oldItem == newItem
        }
    }
}
