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
import java.util.concurrent.CountDownLatch

/**
 * A non-blocking initialization task to be managed by the [AppInitializer].
 * It will be dispatched to a worker thread.
 * */
abstract class AsyncInitializer : Initializer() {
    private val notifier = AsyncNotifierImpl()

    override val isAsync: Boolean = true

    /**
     * Initializes with the given application [Context].
     *
     * @param context Application context.
     * @param notifier Use it to signal that the async task is finished.
     * */
    open fun init(context: Context, notifier: AsyncNotifier) {}

    override fun init(context: Context) {
        init(context, notifier)
        notifier.await()
    }

    interface AsyncNotifier {
        fun finish()
    }

    private class AsyncNotifierImpl : AsyncNotifier {
        private val latch = CountDownLatch(1)

        override fun finish() {
            latch.countDown()
        }

        fun await() = latch.await()
    }
}
