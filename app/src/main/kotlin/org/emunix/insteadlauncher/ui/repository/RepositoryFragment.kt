/*
 * Copyright (c) 2018-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.repository

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.data.Game
import org.emunix.insteadlauncher.helpers.insetDivider
import org.emunix.insteadlauncher.helpers.visible
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.emunix.insteadlauncher.databinding.FragmentRepositoryBinding
import org.emunix.insteadlauncher.helpers.showToast
import org.emunix.insteadlauncher.ui.launcher.AppArgumentViewModel

private const val READ_REQUEST_CODE = 546

class RepositoryFragment : Fragment() {
    private val viewModel: RepositoryViewModel by viewModels()
    private val appArgumentViewModel: AppArgumentViewModel by activityViewModels()
    private lateinit var installDialog: ProgressDialog

    private var _binding: FragmentRepositoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var listAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRepositoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init()

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.list.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(binding.list.context, LinearLayout.VERTICAL)
        val insetDivider = dividerItemDecoration.insetDivider(binding.list.context, R.dimen.installed_game_fragment_inset_divider_margin_start)
        dividerItemDecoration.setDrawable(insetDivider)
        binding.list.addItemDecoration(dividerItemDecoration)
        listAdapter = RepositoryAdapter { game, image ->
            val bundle = bundleOf("game_name" to game.name)
            findNavController().navigate(R.id.action_repositoryFragment_to_gameFragment, bundle)
        }
        listAdapter.setHasStableIds(true)
        binding.list.adapter = listAdapter
        binding.list.setHasFixedSize(true)

        binding.tryAgainButton.setOnClickListener { viewModel.updateRepository() }

        binding.swipeToRefresh.setOnRefreshListener { viewModel.updateRepository() }

        viewModel.getGames().observe(viewLifecycleOwner) { games ->
            if (games != null) {
                if (!games.isEmpty()) {
                    showGames(games)
                } else {
                    viewModel.updateRepository()
                }
            }
        }

        viewModel.getProgressState().observe(viewLifecycleOwner) { state ->
            if (state != null) {
                binding.swipeToRefresh.isRefreshing = state
            }
        }

        viewModel.getErrorViewState().observe(viewLifecycleOwner) { state ->
            if (state != null)
                binding.errorView.visible(state)
        }

        viewModel.getGameListState().observe(viewLifecycleOwner) { state ->
            if (state != null)
                binding.list.visible(state)
        }

        installDialog = ProgressDialog(activity)
        installDialog.setMessage(activity?.getString(R.string.notification_install_game))
        installDialog.setCancelable(false)
        installDialog.setCanceledOnTouchOutside(false)

        viewModel.getInstallGameDialogState().observe(viewLifecycleOwner) { state ->
            if (state != null) {
                if (state == true)
                    installDialog.show()
                else
                    installDialog.cancel()
            }
        }

        viewModel.getSnackbarMessage().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resId ->
                Snackbar.make(binding.list, getString(resId), Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.getToastMessage().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resId ->
                context?.showToast(getString(resId))
            }
        }

        appArgumentViewModel.zipUri.observe(viewLifecycleOwner) { zipUri ->
            zipUri?.let { uri ->
                viewModel.installGame(uri)
                appArgumentViewModel.zipUri.value = null
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_repository, menu)

        val searchView = menu.findItem(R.id.action_search)!!.actionView as SearchView
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
                val query = "%$text%"
                viewModel.searchGames(query).observe(this@RepositoryFragment) { games ->
                    if (games != null) {
                        showGames(games)
                        binding.nothingFoundText.visible(games.isEmpty())
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

    private fun showGames(games: List<Game>) {
        val sortedGames = games.sortedByDescending { it.date }
        listAdapter.submitList(sortedGames)
    }
}
