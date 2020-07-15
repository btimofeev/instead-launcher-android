/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.repository.parser

import org.emunix.insteadlauncher.data.Game

interface GameListParser {

    fun parse(input: String): Map<String, Game>

}