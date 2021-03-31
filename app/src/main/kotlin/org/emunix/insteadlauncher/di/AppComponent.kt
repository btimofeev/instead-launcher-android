/*
 * Copyright (c) 2020-2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import android.content.SharedPreferences
import dagger.Component
import org.emunix.instead.core_storage_api.data.Storage
import org.emunix.insteadlauncher.data.GameDatabase
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.services.InstallGame
import org.emunix.insteadlauncher.services.ScanGames
import org.emunix.insteadlauncher.services.UpdateRepository
import org.emunix.insteadlauncher.services.UpdateRepositoryWorker
import org.emunix.insteadlauncher.ui.about.AboutFragment
import org.emunix.insteadlauncher.ui.game.GameViewModel
import org.emunix.insteadlauncher.ui.installedgames.InstalledGamesViewModel
import org.emunix.insteadlauncher.ui.settings.ThemeListPreference
import org.emunix.insteadlauncher.ui.unpackresources.UnpackResourcesViewModel
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DatabaseModule::class, NetworkModule::class, PreferenceModule::class])
interface AppComponent {

    fun inject(service: UpdateRepository)
    fun inject(service: InstallGame)
    fun inject(service: ScanGames)
    fun inject(fragment: AboutFragment)
    fun inject(preference: ThemeListPreference)
    fun inject(worker: UpdateRepositoryWorker)
    fun inject(viewModel: UnpackResourcesViewModel)
    fun inject(viewModel: GameViewModel)
    fun inject(viewModel: InstalledGamesViewModel)

    fun sharedPreferences(): SharedPreferences
    fun db(): GameDatabase
    fun storage(): Storage
    fun eventBus(): EventBus
}