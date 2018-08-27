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

class InstalledGamesAdapter(val onClickListener: (Game) -> Unit) : RecyclerView.Adapter<InstalledGamesAdapter.ViewHolder>() {
    private var items: List<Game> = emptyList()

    private lateinit var longClickedGame: Game

    fun getLongClickedGame(): Game = longClickedGame

    fun loadItems(newItems: List<Game>) {
        items = newItems
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
        val image = itemView.findViewById<ImageView>(R.id.image)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_installed_games_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = items[position].title
        holder.image.loadUrl(items[position].image)
        holder.itemView.setOnClickListener { onClickListener(items[position]) }
        holder.itemView.setOnLongClickListener {
            longClickedGame = items[position]
            false
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = items.size

}
