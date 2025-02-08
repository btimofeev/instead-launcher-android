/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.domain.usecase.SearchGamesUseCase
import org.emunix.insteadlauncher.presentation.models.SearchScreenState
import org.emunix.insteadlauncher.presentation.models.toRepoGames
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGamesUseCase: SearchGamesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<SearchScreenState>(SearchScreenState.Empty)

    val state = _state.asStateFlow()

    fun searchGames(query: String) = viewModelScope.launch {
        if (query.isBlank()) {
            _state.value = SearchScreenState.Empty
            return@launch
        }

        val games = searchGamesUseCase(query)
        if (games.isEmpty()) {
            _state.value = SearchScreenState.NothingFound
        } else {
            _state.value = SearchScreenState.Result(games.toRepoGames())
        }
    }
}
