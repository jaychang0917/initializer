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

@file:Suppress("MagicNumber")

package com.jaychang.initializer.example

import android.content.Context
import com.jaychang.initializer.AsyncInitializer
import com.jaychang.initializer.Initializer
import kotlin.concurrent.thread

class InitializerA : Initializer() {
    override fun init(context: Context) {
        println("Init A finished.")
    }

    override fun dependencies(): List<Class<out Initializer>> = emptyList()
}

class AsyncInitializerA1 : AsyncInitializer() {
    override fun init(context: Context, notifier: AsyncNotifier) {
        // Run on worker thread.
        Thread.sleep(1000)
        notifier.finish()
        println("Init async A1 finished.")
    }

    override fun dependencies(): List<Class<out Initializer>> = listOf(InitializerA::class.java)
}

class LateAsyncInitializerA2 : AsyncInitializer() {
    // Late init, allow to finish initialization after first frame.
    override val isLate: Boolean = true

    override fun init(context: Context, notifier: AsyncNotifier) {
        // Run on worker thread.
        println("Late init async A2 start.")
        thread {
            Thread.sleep(5000)
            notifier.finish()
            println("Late init async A2 finished.")
        }
        println("Late init async A2 wait...")
    }

    override fun dependencies(): List<Class<out Initializer>> = listOf(InitializerA::class.java)
}

class LateInitializerB : Initializer() {
    // Late init, allow to finish initialization after first frame.
    override val isLate: Boolean = true

    override fun init(context: Context) {
        println("Late init B finished.")
    }

    override fun dependencies(): List<Class<out Initializer>> = listOf(
        AsyncInitializerA1::class.java,
        LateAsyncInitializerA2::class.java,
    )
}
