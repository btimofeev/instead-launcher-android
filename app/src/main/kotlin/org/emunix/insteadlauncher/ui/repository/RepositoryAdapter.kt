package org.emunix.insteadlauncher.ui.repository

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_repository_list_item.view.*
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.inflate
import org.emunix.insteadlauncher.helpers.loadUrl
import org.emunix.insteadlauncher.helpers.visible

class RepositoryAdapter(val items: List<Game>, val listener: (Game) -> Unit): RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.activity_repository_list_item))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], listener)

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Game, listener: (Game) -> Unit) = with(itemView) {
            name.text = item.title
            image.loadUrl(item.image)
            description.text = item.brief
            setOnClickListener { listener(item) }

            if (item.installedVersion.isNotBlank() and (item.version != item.installedVersion)) {
                badge.visible(true)
            }
        }
    }
}
