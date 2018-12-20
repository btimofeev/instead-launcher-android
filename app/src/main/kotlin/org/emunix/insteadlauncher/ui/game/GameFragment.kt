package org.emunix.insteadlauncher.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import org.emunix.insteadlauncher.services.DeleteGame
import org.emunix.insteadlauncher.services.InstallGame
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
        activity.supportActionBar?.title = game.title

        name.text = game.title
        author.text = game.author
        if (game.installedVersion.isNotBlank() and (game.version != game.installedVersion)){
            version.text = getString(R.string.game_activity_label_version, game.installedVersion + " (\u2191${game.version})")
        } else {
            version.text = getString(R.string.game_activity_label_version, game.version)
        }
        size.text = getString(R.string.game_activity_label_size, FileUtils.byteCountToDisplaySize(game.size))
        image.loadUrl(game.image)
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
            installButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.colorPrimary)
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
            val installGame = Intent(activity, InstallGame::class.java)
            installGame.putExtra("game_url", game.url)
            installGame.putExtra("game_name", game.name)
            activity.startService(installGame)
            GlobalScope.launch(Dispatchers.IO) { game.saveStateToDB(IN_QUEUE_TO_INSTALL) }
        }

        deleteButton.setOnClickListener {
            if (game.state == INSTALLED) {
                val dialog = AlertDialog.Builder(activity, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                dialog.setTitle(R.string.dialog_delete_game_title)
                dialog.setMessage(R.string.dialog_delete_game_text)
                dialog.setPositiveButton(R.string.dialog_delete_game_positive_button) { _,_ ->
                    val deleteGame = Intent(activity, DeleteGame::class.java)
                    deleteGame.putExtra("game_name", game.name)
                    activity.startService(deleteGame)
                }
                dialog.setNegativeButton(R.string.dialog_delete_game_negative_button) { dialog, _ ->
                    dialog.cancel()
                }
                dialog.create()
                dialog.show()
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
