/*
 * Copyright (c) 2020-2022 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.unpackresources

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.BuildConfig
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.ERROR
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.NO_UPDATE_REQUIRED
import org.emunix.insteadlauncher.domain.usecase.UpdateResourceUseCase.UpdateResult.SUCCESS
import javax.inject.Inject

@HiltViewModel
class UnpackResourcesViewModel @Inject constructor(
    private val updateResourceUseCase: UpdateResourceUseCase,
) : ViewModel() {

    private var _unpackSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    private var _showError: MutableLiveData<Boolean> = MutableLiveData(false)

    val unpackSuccess: LiveData<Boolean> = _unpackSuccess
    val showError: LiveData<Boolean> = _showError

    init {
        update()
    }

    fun tryAgainIsClicked() {
        update()
    }

    private fun update() = viewModelScope.launch {
        _showError.value = false
        when (updateResourceUseCase(forceUpdate = BuildConfig.DEBUG)) {
            SUCCESS, NO_UPDATE_REQUIRED -> _unpackSuccess.value = true
            ERROR -> _showError.value = true
        }
    }
}