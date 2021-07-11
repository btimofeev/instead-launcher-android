/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.helpers.gameparser.GameParser
import org.emunix.insteadlauncher.helpers.gameparser.GameParserImpl
import org.emunix.insteadlauncher.helpers.gamesdirscanner.GamesDirScanner
import org.emunix.insteadlauncher.helpers.gamesdirscanner.GamesDirScannerImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface GameUtilsModule {

    @Singleton
    @Binds
    fun bindGameParser(impl: GameParserImpl): GameParser

    @Singleton
    @Binds
    fun bindGamesDirScanner(impl: GamesDirScannerImpl): GamesDirScanner
}