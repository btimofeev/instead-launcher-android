/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.resourceprovider

import androidx.annotation.StringRes

interface ResourceProvider {

    /**
     * Returns a localized string from application resources
     *
     * @param stringResId string id (example, R.string.some_string_name)
     * @return The string data associated with the resource, stripped of styled
     *         text information.
     */
    fun getString(@StringRes stringResId: Int): String

    /**
     * Returns a localized string from application resources
     *
     * @param stringResId string id (example, R.string.some_string_name)
     * @param args The format arguments that will be used for
     *                   substitution.
     * @return The string data associated with the resource, stripped of styled
     *         text information.
     */
    fun getString(@StringRes stringResId: Int, vararg args: Any): String
}