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

import com.jaychang.initializer.Initializer
import java.util.concurrent.CountDownLatch

internal class AppInitializerLatch(private val waiters: List<Initializer>) {
    private val latch = CountDownLatch(waiters.size)

    fun markDone(initializer: Initializer) {
        if (waiters.isEmpty()) return

        if (waiters.contains(initializer)) {
            latch.countDown()
        }
    }

    fun await() {
        if (waiters.isEmpty()) return

        latch.await()
    }
}
