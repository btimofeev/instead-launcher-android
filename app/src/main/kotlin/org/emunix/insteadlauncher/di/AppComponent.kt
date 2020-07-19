/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import android.content.SharedPreferences
import dagger.Component
import org.emunix.insteadlauncher.data.GameDatabase
import org.emunix.insteadlauncher.helpers.eventbus.EventBus
import org.emunix.insteadlauncher.services.*
import org.emunix.insteadlauncher.storage.Storage
import org.emunix.insteadlauncher.ui.about.AboutFragment
import org.emunix.insteadlauncher.ui.instead.InsteadActivity
import org.emunix.insteadlauncher.ui.settings.ThemeListPreference
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DatabaseModule::class, NetworkModule::class, PreferenceModule::class, StorageModule::class])
interface AppComponent {

    fun inject(service: UpdateRepository)
    fun inject(service: UpdateResources)
    fun inject(service: InstallGame)
    fun inject(service: ScanGames)
    fun inject(activity: InsteadActivity)
    fun inject(fragment: AboutFragment)
    fun inject(preference: ThemeListPreference)
    fun inject(worker: UpdateRepositoryWorker)

    fun sharedPreferences(): SharedPreferences
    fun db(): GameDatabase
    fun storage(): Storage
    fun eventBus(): EventBus
}