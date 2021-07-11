/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.unpackresources

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.helpers.ResourceUpdater
import javax.inject.Inject

@HiltViewModel
class UnpackResourcesViewModel @Inject constructor(
    private val resourceUpdater: ResourceUpdater
) : ViewModel() {

    private var unpackSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    private var showError: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        update()
    }

    fun tryAgainIsClicked() {
        update()
    }

    private fun update() = viewModelScope.launch {
        showError.value = false
        if (resourceUpdater.update())
            unpackSuccess.value = true
        else
            showError.value = true
    }

    fun getUnpackSuccessStatus(): LiveData<Boolean> = unpackSuccess

    fun getErrorStatus(): LiveData<Boolean> = showError
}