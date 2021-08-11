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
import com.jaychang.initializer.internal.AppInitializerMediator

/**
 * An initialization task to be managed by the [AppInitializer].
 * */
abstract class Initializer : Runnable {
    internal lateinit var mediator: AppInitializerMediator
    internal open val isAsync: Boolean = false
    internal val beforeInitializers = mutableListOf<Initializer>()
    internal val afterInitializers = mutableListOf<Initializer>()

    /**
     * A lazy initializer doesn't need to be finished before the app's first frame.
     *
     * The [AppInitializer] will wait for all non-lazy initializers to be finished.
     *
     * Default `false`.
     * */
    open val isLazy: Boolean = false

    /**
     * Initializes with the given application [Context].
     * */
    abstract fun init(context: Context)

    /**
     * The dependent tasks that need to be finished before running this task.
     * */
    abstract fun dependencies(): List<Class<out Initializer>>

    override fun run() {
        mediator.start(this)
        init(mediator.appContext)
        mediator.finish(this)
    }

    internal fun start() {
        mediator.execute(this)
    }

    internal fun addAfter(initializer: Initializer) {
        afterInitializers.add(initializer)
    }

    internal fun addBefore(initializer: Initializer) {
        beforeInitializers.add(initializer)
    }

    override fun toString(): String {
        return javaClass.name
    }
}
