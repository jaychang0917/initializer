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

package com.jaychang.initializer

import android.content.Context
import android.os.Looper
import androidx.annotation.MainThread
import com.jaychang.initializer.internal.AppInitializerBarrier
import com.jaychang.initializer.internal.AppInitializerMediator
import com.jaychang.initializer.internal.AppInitializerExecutor
import com.jaychang.initializer.internal.AppInitializerGraph
import com.jaychang.initializer.internal.AppInitializerLatch

/**
 * A facade to initialize all input [Initializer]s.
 * */
class AppInitializer private constructor(
    private val appContext: Context,
    private val listeners: List<InitializationListener>
) {
    private lateinit var mediator: AppInitializerMediator

    @MainThread
    fun init(vararg initializers: Initializer) {
        init(initializers.toList())
    }

    @MainThread
    fun init(initializers: List<Initializer>) {
        ensureMainThread()

        if (initializers.isEmpty()) return

        val graph = AppInitializerGraph(initializers)
        val waiters = graph.vertexes.filter { !it.isLazy }
        val latch = AppInitializerLatch(waiters)
        mediator = AppInitializerMediator(
            appContext = appContext,
            graph = graph,
            executor = AppInitializerExecutor(graph.vertexes),
            waitersLatch = latch,
            doneBarrier = AppInitializerBarrier(graph.vertexes),
            listeners = listeners
        )
        mediator.prepare()
        mediator.startAndAwait()
    }

    private fun ensureMainThread() {
        check(Thread.currentThread() == Looper.getMainLooper().thread) {
            "AppInitializer#wait should be invoked on MainThread."
        }
    }

    class Builder(private val appContext: Context) {
        private var listeners = mutableListOf<InitializationListener>()

        fun addListener(listener: InitializationListener): Builder {
            listeners.add(listener)
            return this
        }

        fun build(): AppInitializer {
            return AppInitializer(
                appContext = appContext,
                listeners = listeners
            )
        }
    }
}
