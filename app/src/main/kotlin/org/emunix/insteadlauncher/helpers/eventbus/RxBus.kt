/*
 * Copyright (c) 2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.helpers.eventbus

// code from https://android.jlelse.eu/super-simple-event-bus-with-rxjava-and-kotlin-f1f969b21003

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

// Use object so we have a singleton instance
object RxBus: EventBus {

    private val publisher = PublishSubject.create<Any>()

    override fun publish(event: Any) {
        publisher.onNext(event)
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    override fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

}

