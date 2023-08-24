/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.insteadlauncher.domain.parser.GameParser
import org.emunix.insteadlauncher.domain.parser.GameParserImpl

@InstallIn(SingletonComponent::class)
@Module
interface GameUtilsModule {

    @Binds
    fun bindGameParser(impl: GameParserImpl): GameParser
}