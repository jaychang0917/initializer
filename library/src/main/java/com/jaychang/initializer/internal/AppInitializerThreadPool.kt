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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

internal class AppInitializerThreadPool(private val initializers: List<Initializer>) {
    private val pool: ThreadPoolExecutor

    init {
        pool = buildThreadPool()
    }

    @Suppress("MagicNumber")
    private fun buildThreadPool(): ThreadPoolExecutor {
        val poolSize = poolSize()
        val threadFactory = ThreadFactory {
            val count = AtomicInteger(1)
            Thread(it, "AppInitializerThread#${count.getAndIncrement()}")
        }
        return ThreadPoolExecutor(
            poolSize.core,
            poolSize.max,
            0L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(MAX_THREADS_SIZE),
            threadFactory
        )
    }

    @Suppress("MagicNumber")
    private fun poolSize(): PoolSize {
        val cpu = initializers.count { !it.isAsync }
        val io = initializers.count { it.isAsync }
        // CPU : IO
        val cpuRatio = cpu.toDouble() / (cpu + io)
        val isCpuBound = cpuRatio > 0.8
        return if (isCpuBound) {
            PoolSize(CPU_COUNT + 1, CPU_COUNT * 2 + 1)
        } else {
            val size = CPU_COUNT * (io / cpu + 1)
            PoolSize(size, size)
        }
    }

    fun submit(task: Runnable) {
        pool.execute(task)
    }

    private data class PoolSize(val core: Int, val max: Int)

    private companion object {
        private const val MAX_THREADS_SIZE = 128
    }
}
