/*
 *  Copyright (C) 2021. Jay Chang
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaychang.initializer.internal

import android.content.Context
import com.jaychang.initializer.InitializationListener
import com.jaychang.initializer.Initializer

/**
 * Coordinates the
 * */
@Suppress("LongParameterList")
internal class AppInitializerMediator(
    val appContext: Context,
    private val graph: AppInitializerGraph,
    private val executor: AppInitializerExecutor,
    private val waitersLatch: AppInitializerLatch,
    private val doneBarrier: AppInitializerBarrier,
    private val listeners: List<InitializationListener>
) {
    fun prepare() {
        val allInitializer = graph.vertexes
        for (initializer in allInitializer) {
            initializer.mediator = this
        }
    }

    fun startAndAwait() {
        graph.start()
        waitersLatch.await()
    }

    @Synchronized
    fun execute(initializer: Initializer) = executor.execute(initializer)

    @Synchronized
    fun start(initializer: Initializer) {
        if (!doneBarrier.isStarted()) {
            listeners.forEach { it.beforeAll() }
        }
        if (initializer !is AppInitializerGraph.Root) {
            listeners.forEach { it.beforeEach(initializer) }
        }
    }

    @Synchronized
    fun finish(initializer: Initializer) {
        // Unblock if all initializers in the waiting list are done.
        waitersLatch.markDone(initializer)
        doneBarrier.markDone()
        if (initializer !is AppInitializerGraph.Root) {
            listeners.forEach { it.afterEach(initializer) }
            if (doneBarrier.isAllDone()) {
                listeners.forEach { it.afterAll() }
            }
        }
        notifyAfterInitializers(initializer)
    }

    private fun notifyAfterInitializers(done: Initializer) {
        for (after in done.afterInitializers) {
            after.beforeInitializers.remove(done)
            // My before initializers are all done, I can start now.
            if (after.beforeInitializers.isEmpty()) {
                after.start()
            }
        }
    }
}
