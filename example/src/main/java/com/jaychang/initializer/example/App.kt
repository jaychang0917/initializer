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

package com.jaychang.initializer.example

import android.app.Application
import com.jaychang.initializer.AppInitializer
import com.jaychang.initializer.InitializationListener
import com.jaychang.initializer.Initializer

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        println("App onCreate")

        // InitializerA -> AsyncInitializerA1 / LazyAsyncInitializerA2 -> LazyInitializerB
        val initializers = listOf(InitializerA(), LazyAsyncInitializerA2(), LazyInitializerB(), AsyncInitializerA1())
        val appInitializer = AppInitializer.Builder(this)
            .addListener(object : InitializationListener {
                override fun beforeAll() {
                    println("beforeAll")
                }

                override fun beforeEach(initializer: Initializer) {
                    println("beforeEach: ${initializer::class.java.simpleName}")
                }

                override fun afterEach(initializer: Initializer) {
                    println("afterEach: ${initializer::class.java.simpleName}")
                }

                override fun afterAll() {
                    println("afterAll")
                }
            })
            .build()
        appInitializer.init(initializers)

        println("App Initialized")
    }
}
