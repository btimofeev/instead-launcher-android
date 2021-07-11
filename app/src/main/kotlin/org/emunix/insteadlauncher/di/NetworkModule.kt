/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.emunix.insteadlauncher.repository.fetcher.GameListFetcher
import org.emunix.insteadlauncher.repository.fetcher.InsteadGamesXmlFetcher
import org.emunix.insteadlauncher.repository.parser.GameListParser
import org.emunix.insteadlauncher.repository.parser.InsteadGamesXmlParser
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideGameListFetcher(client: OkHttpClient): GameListFetcher = InsteadGamesXmlFetcher(client)

    @Provides
    @Singleton
    fun provideGameListParser(): GameListParser = InsteadGamesXmlParser()
}