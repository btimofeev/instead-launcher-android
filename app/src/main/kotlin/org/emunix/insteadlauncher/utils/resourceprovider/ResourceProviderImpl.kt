/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils.resourceprovider

import android.content.Context
import javax.inject.Inject

class ResourceProviderImpl @Inject constructor(private val context: Context) : ResourceProvider {

    override fun getString(stringResId: Int): String = context.getString(stringResId)

    override fun getString(stringResId: Int, vararg args: Any): String = context.getString(stringResId, *args)
}