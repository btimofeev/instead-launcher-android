/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.ui.installedgames

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.emunix.insteadlauncher.InsteadLauncher
import org.emunix.insteadlauncher.helpers.ResourceUpdater
import javax.inject.Inject

class UnpackResourcesViewModel(var app: Application): AndroidViewModel(app) {

    @Inject lateinit var resourceUpdater: ResourceUpdater

    private var unpackSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    private var showError: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        InsteadLauncher.appComponent.inject(this)
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