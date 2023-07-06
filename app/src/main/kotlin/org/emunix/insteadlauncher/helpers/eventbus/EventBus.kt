/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.eventbus

import io.reactivex.rxjava3.core.Observable

interface EventBus {
    fun publish(event: Any)
    fun <T : Any> listen(eventType: Class<T>): Observable<T>
}