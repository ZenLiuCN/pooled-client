/*
 * Copyright 2013 Maurício Linhares
 *
 * Maurício Linhares licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.github.mauricio.async.db.pool

import com.github.elizarov.async.suspendable
import kotlin.coroutines.ContinuationDispatcher

/**
 *
 * Defines the common interface for com.github.mauricio.async.db object pools. These are pools that do not block clients trying to acquire
 * a resource from it. Different than the usual synchronous pool, you **must** return objects back to it manually
 * since it's impossible for the pool to know when the object is ready to be given back.
 *
 * @tparam T
 */

interface AsyncObjectPool<T> {

    /**
     *
     * Returns an object from the pool to the callee with the returned future. If the pool can not create or enqueue
     * requests it will fill the returned [[scala.concurrent.Future]] with an
     * [[com.github.mauricio.async.db.pool.PoolExhaustedException]].
     *
     * @return future that will eventually return a usable pool object.
     */

    suspend fun take(): T

    /**
     *
     * Returns an object taken from the pool back to it. This object will become available for another client to use.
     * If the object is invalid or can not be reused for some reason the [[scala.concurrent.Future]] returned will contain
     * the error that prevented this object of being added back to the pool. The object is then discarded from the pool.
     *
     * @param item
     * @return
     */

    suspend fun giveBack(item: T): AsyncObjectPool<T>

    /**
     *
     * Closes this pool and future calls to **take** will cause the [[scala.concurrent.Future]] to raise an
     * [[com.github.mauricio.async.db.pool.PoolAlreadyTerminatedException]].
     *
     * @return
     */

    suspend fun close(): AsyncObjectPool<T>

    /**
     *
     * Retrieve and use an object from the pool for a single computation, returning it when the operation completes.
     *
     * @param f function that uses the object
     * @return f wrapped with take and giveBack
     */

    //sorry, no use here
    suspend fun <A> use(dispatcher: ContinuationDispatcher? = null, f: suspend (t: T) -> A) : A = suspendable(dispatcher) {
        val item = take()

        try {
            return@suspendable f(item)
        } finally {
            giveBack(item)
        }
    }

}
