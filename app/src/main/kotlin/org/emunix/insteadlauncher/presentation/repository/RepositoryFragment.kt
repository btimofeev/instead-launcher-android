/*
 * Copyright (c) 2018-2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.repository

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.R.dimen
import org.emunix.insteadlauncher.R.drawable
import org.emunix.insteadlauncher.R.string
import org.emunix.insteadlauncher.databinding.FragmentRepositoryBinding
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.models.ErrorDialogModel
import org.emunix.insteadlauncher.presentation.models.RepoGame
import org.emunix.insteadlauncher.presentation.models.RepoScreenState
import org.emunix.insteadlauncher.presentation.models.RepoScreenState.SEARCH_ERROR
import org.emunix.insteadlauncher.presentation.models.RepoScreenState.SHOW_GAMES
import org.emunix.insteadlauncher.presentation.models.RepoScreenState.UPDATE_REPOSITORY
import org.emunix.insteadlauncher.presentation.models.RepoScreenState.UPDATE_REPOSITORY_ERROR
import org.emunix.insteadlauncher.utils.insetDivider

private const val READ_REQUEST_CODE = 546

@AndroidEntryPoint
class RepositoryFragment : Fragment(R.layout.fragment_repository) {

    private val viewModel: RepositoryViewModel by viewModels()

    private val installDialog: ProgressDialog by lazy(mode = LazyThreadSafetyMode.NONE) {
        ProgressDialog(activity).apply {
            setMessage(activity?.getString(string.notification_install_game))
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private val binding by viewBinding(FragmentRepositoryBinding::bind)

    private lateinit var listAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenu()
        viewModel.init()
        setupViews()
        setupObservers()
        handleApplicationZipArgument()
    }

    private fun initMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_repository, menu)

                val searchView = menu.findItem(R.id.action_search)?.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String): Boolean {
                        search(newText)
                        return true
                    }

                    override fun onQueryTextSubmit(query: String): Boolean {
                        search(query)
                        return false
                    }

                    fun search(text: String) {
                        viewModel.searchGames(text)
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_update_repo -> {
                        viewModel.updateRepository()
                        return true
                    }

                    R.id.action_install_local_game -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "application/zip"
                        startActivityForResult(intent, READ_REQUEST_CODE, null)
                        return true
                    }
                }
                return false
            }

        }, viewLifecycleOwner, State.RESUMED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null) {
            val uri = resultData.data
            if (uri != null) {
                viewModel.installGame(uri)
            }
        }
    }

    private fun setupViews() {
        setupToolbar()
        setupGameList()
        binding.tryAgainButton.setOnClickListener { viewModel.updateRepository() }
        binding.swipeToRefresh.setOnRefreshListener { viewModel.updateRepository() }
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(drawable.ic_back_24dp)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupGameList() {
        binding.list.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        setupGameListDecoration()
        setupGameListAdapter()
        binding.list.setHasFixedSize(true)
    }

    private fun setupGameListDecoration() {
        val dividerItemDecoration =
            DividerItemDecoration(binding.list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(
            context = binding.list.context,
            start_offset_dimension = dimen.installed_game_fragment_inset_divider_margin_start
        )
        dividerItemDecoration.setDrawable(insetDivider)
        binding.list.addItemDecoration(dividerItemDecoration)
    }

    private fun setupGameListAdapter() {
        listAdapter = RepositoryAdapter { game, image ->
            val bundle = bundleOf("game_name" to game.name)
            findNavController().navigate(R.id.action_repositoryFragment_to_gameFragment, bundle)
        }
        listAdapter.setHasStableIds(true)
        binding.list.adapter = listAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(State.STARTED) {
                launch {
                    viewModel.gameItems.collect { games ->
                        showGames(games)
                    }
                }

                launch {
                    viewModel.showErrorDialog.collect { data ->
                        showErrorDialog(data)
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        setUiState(state)
                    }
                }

                launch {
                    viewModel.showInstallGameDialog.collect { isShow ->
                        showInstallGameDialog(isShow)
                    }
                }
            }
        }
    }

    private fun setUiState(state: RepoScreenState) {
        when (state) {
            SHOW_GAMES -> {
                with(binding) {
                    list.isVisible = true
                    errorView.isVisible = false
                    swipeToRefresh.isRefreshing = false
                    nothingFoundText.isVisible = false
                }

            }

            UPDATE_REPOSITORY -> {
                with(binding) {
                    list.isVisible = false
                    errorView.isVisible = false
                    swipeToRefresh.isRefreshing = true
                    nothingFoundText.isVisible = false
                }
            }

            UPDATE_REPOSITORY_ERROR -> {
                with(binding) {
                    list.isVisible = false
                    errorView.isVisible = true
                    swipeToRefresh.isRefreshing = false
                    nothingFoundText.isVisible = false
                }
            }

            SEARCH_ERROR -> {
                with(binding) {
                    list.isVisible = false
                    errorView.isVisible = false
                    swipeToRefresh.isRefreshing = false
                    binding.nothingFoundText.isVisible = true
                }
            }
        }
    }

    private fun handleApplicationZipArgument() {
        val appArgumentViewModel: AppArgumentViewModel by activityViewModels()
        appArgumentViewModel.zipUri?.let { uri ->
            viewModel.installGame(uri)
            appArgumentViewModel.zipUri = null
        }
    }

    private fun showGames(games: List<RepoGame>) {
        listAdapter.submitList(games)
    }

    private fun showErrorDialog(data: ErrorDialogModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(data.title)
            .setMessage(data.message)
            .setPositiveButton(string.dialog_error_close_button) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showInstallGameDialog(isShow: Boolean) {
        if (isShow) {
            installDialog.show()
        } else {
            installDialog.cancel()
        }
    }
}
