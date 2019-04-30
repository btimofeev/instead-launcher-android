/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.data

import androidx.recyclerview.widget.DiffUtil


class GameDiffCallback : DiffUtil.ItemCallback<Game>() {

    enum class Diff {
        IMAGE, TITLE, BRIEF, VERSION
    }

    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.name === newItem.name
    }

    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Game, newItem: Game): List<Diff> {
        val diff = mutableListOf<Diff>()

        if (newItem.image != oldItem.image)
            diff.add(Diff.IMAGE)

        if (newItem.title != oldItem.title)
            diff.add(Diff.TITLE)

        if (newItem.brief != oldItem.brief)
            diff.add(Diff.BRIEF)

        if (newItem.version != oldItem.version || newItem.installedVersion != oldItem.installedVersion)
            diff.add(Diff.VERSION)

        return diff
    }
}