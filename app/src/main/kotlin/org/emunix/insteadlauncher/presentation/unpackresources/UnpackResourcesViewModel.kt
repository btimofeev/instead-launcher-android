/*
 * Copyright (c) 2020-2023 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.unpackresources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.BuildConfig
import org.emunix.insteadlauncher.domain.usecase.CreateDirectoriesUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.ERROR
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.NO_UPDATE_REQUIRED
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.SUCCESS
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState
import org.emunix.insteadlauncher.presentation.models.UnpackResourcesScreenState.UNPACKING
import org.emunix.insteadlauncher.utils.writeToLog
import javax.inject.Inject

@HiltViewModel
class UnpackResourcesViewModel @Inject constructor(
    private val updateResourceUseCase: UpdateResourceUseCase,
    private val createDirectoriesUseCase: CreateDirectoriesUseCase,
) : ViewModel() {

    private val _screenState = MutableStateFlow(UNPACKING)

    val screenState: StateFlow<UnpackResourcesScreenState> = _screenState

    init {
        update()
    }

    fun tryAgainIsClicked() {
        update()
    }

    private fun update() = viewModelScope.launch {
        _screenState.value = UNPACKING
        runCatching {
            createDirectoriesUseCase()
            when (updateResourceUseCase(forceUpdate = BuildConfig.DEBUG)) {
                SUCCESS, NO_UPDATE_REQUIRED -> _screenState.value = UnpackResourcesScreenState.SUCCESS
                ERROR -> _screenState.value = UnpackResourcesScreenState.ERROR
            }
        }.onFailure { err ->
            err.writeToLog()
            _screenState.value = UnpackResourcesScreenState.ERROR
        }
    }
}