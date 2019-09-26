/*
 * Copyright (c) 2018-2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_repository.*
import org.emunix.insteadlauncher.R
import kotlinx.android.synthetic.main.fragment_repository.*
import android.app.Activity
import android.transition.Slide
import android.view.Gravity
import android.view.Window


private const val READ_REQUEST_CODE = 546

class RepositoryActivity : AppCompatActivity() {
    private lateinit var viewModel: RepositoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Slide(Gravity.END)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.repository_activity_title)

        viewModel = ViewModelProviders.of(this).get(RepositoryViewModel::class.java)
        viewModel.init()

        swipe_to_refresh.setOnRefreshListener {
            viewModel.updateRepository()
        }

        val intent = intent
        if (intent.type == "application/zip") {
            val uri = intent.data
            if (uri != null)
                viewModel.installGame(uri)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repository, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_update_repo -> {
                viewModel.updateRepository()
                return true
            }
            R.id.action_install_local_game -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/zip"
                ActivityCompat.startActivityForResult(this, intent, READ_REQUEST_CODE, null)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                if (uri != null)
                    viewModel.installGame(uri)
            }
        }
    }
}
