/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.data.Game.State.*
import org.emunix.insteadlauncher.helpers.loadUrl
import org.emunix.insteadlauncher.helpers.saveStateToDB
import org.emunix.insteadlauncher.helpers.visible
import org.emunix.insteadlauncher.services.InstallGame
import org.emunix.insteadlauncher.ui.dialogs.DeleteGameDialog
import org.emunix.insteadlauncher.ui.instead.InsteadActivity

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
            viewModel = ViewModelProviders.of(activity!!).get(GameViewModel::class.java)
            viewModel.init(gameName)
            viewModel.getGame().observe(this, Observer { game ->
                if (game != null) {
                    setViews(game)
                } else {
                    //todo показать ошибку, что такой игры не найдено в базе
                }
            })
            viewModel.getProgress().observe(this, Observer { value ->
                if (value == -1) {
                    setIndeterminateProgress(true)
                } else {
                    setIndeterminateProgress(false, value)
                }
            })
            viewModel.getProgressMessage().observe(this, Observer { msg ->
                setInstallMessage(msg)
            })
        }
    }

    private fun setViews(game: Game) {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.title = ""
        activity.collapsing_toolbar.isTitleEnabled = false

        name.text = game.title
        author.text = game.author
        if (game.installedVersion.isNotBlank() and (game.version != game.installedVersion)){
            version.text = getString(R.string.game_activity_label_version, game.installedVersion + " (\u2191${game.version})")
        } else {
            version.text = getString(R.string.game_activity_label_version, game.version)
        }
        size.text = getString(R.string.game_activity_label_size, FileUtils.byteCountToDisplaySize(game.size))
        activity.toolbar_image.loadUrl(game.image)
        description.text = game.description

        if (game.state == INSTALLED) {
            installMessage.visible(false)
            progressBar.visible(false)
            installButton.visible(false)
            deleteButton.visible(true)
            runButton.visible(true)

            if (game.version != game.installedVersion){
                installButton.text = getText(R.string.game_activity_button_update)
                installButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.colorUpdateButton)
                installButton.visible(true)
            }
        }

        if (game.state == NO_INSTALLED) {
            installButton.text = getText(R.string.game_activity_button_install)
            installButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.colorInstallButton)
            installButton.visible(true)
            deleteButton.visible(false)
            runButton.visible(false)
            progressBar.visible(false)
            installMessage.visible(false)
        }

        if (game.state == IS_INSTALL) {
            installMessage.text = getString(R.string.game_activity_message_installing)
            showProgress(true)
        }

        if (game.state == IS_DELETE) {
            installMessage.text = getString(R.string.notification_delete_game)
            showProgress(true)
        }

        if (game.state == IN_QUEUE_TO_INSTALL) {
            installMessage.text = getString(R.string.game_activity_message_download_pending)
            showProgress(true)
        }

        installButton.setOnClickListener {
            InstallGame.start(activity, game.name, game.url, game.title)
            GlobalScope.launch(Dispatchers.IO) { game.saveStateToDB(IN_QUEUE_TO_INSTALL) }
        }

        deleteButton.setOnClickListener {
            if (game.state == INSTALLED) {
                val deleteDialog = DeleteGameDialog.newInstance(game.name)
                deleteDialog.show(fragmentManager, "delete_dialog")
            }
        }

        runButton.setOnClickListener {
            if (game.state == INSTALLED) {
                val runGame = Intent(activity, InsteadActivity::class.java)
                runGame.putExtra("game_name", game.name)
                startActivity(runGame)
            }
        }
    }

    private fun showProgress(flag: Boolean) {
        installMessage.visible(flag)
        progressBar.visible(flag)
        installButton.visible(!flag)
        deleteButton.visible(!flag)
        runButton.visible(!flag)
    }

    private fun setIndeterminateProgress(indeterminate: Boolean, value: Int = 0){
        if (progressBar.isIndeterminate != indeterminate) {
            progressBar.isIndeterminate = indeterminate
        }
        progressBar.progress = value
    }

    private fun setInstallMessage(msg: String) {
        installMessage.text = msg
    }

}
