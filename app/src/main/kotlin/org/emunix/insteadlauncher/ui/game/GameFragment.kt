package org.emunix.insteadlauncher.ui.game

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_game.*
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.loadUrl
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.services.DeleteGame
import org.emunix.insteadlauncher.services.InstallGame

class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = this.arguments
        if (bundle != null) {
            val gameName = bundle.getString("game_name")
            viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)
            viewModel.init(gameName)
            viewModel.getGame().observe(this, Observer { game ->
                if (game != null) {
                    setViews(game)
                } else {
                    //todo показать ошибку, что такой игры не найдено в базе
                }
            })
        }
    }

    private fun setViews(game: Game) {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.title = game.title

        name.text = game.title
        author.text = game.author
        version.text = getString(R.string.game_activity_label_version, game.version)
        size.text = getString(R.string.game_activity_label_size, FileUtils.byteCountToDisplaySize(game.size))
        image.loadUrl(game.image)
        description.text = game.description

        if (game.installed) {
            installButton.text = getText(R.string.game_activity_button_run)
            deleteButton.visible(true)
            progressBar.visible(false)
            installMessage.visible(false)
        } else {
            installButton.text = getText(R.string.game_activity_button_install)
            deleteButton.visible(false)
        }

        installButton.setOnClickListener({
            if (!game.installed) {
                val installGame = Intent(activity, InstallGame::class.java)
                installGame.putExtra("game_url", game.url)
                installGame.putExtra("game_name", game.name)
                activity.startService(installGame)
                progressBar.visible(true)
                installMessage.visible(true)
            } else {
                // todo run game
            }
        })

        deleteButton.setOnClickListener({
            if (game.installed) {
                val deleteGame = Intent(activity, DeleteGame::class.java)
                deleteGame.putExtra("game_name", game.name)
                activity.startService(deleteGame)
            }
        })
    }
}
