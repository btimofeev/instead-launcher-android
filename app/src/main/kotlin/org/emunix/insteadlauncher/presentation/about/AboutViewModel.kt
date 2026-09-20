package org.emunix.insteadlauncher.presentation.about

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.emunix.insteadlauncher.domain.repository.AppVersionRepository
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
) : ViewModel() {

    private val _appVersion = MutableStateFlow(appVersionRepository.versionName)

    val appVersion = _appVersion.asStateFlow()
}