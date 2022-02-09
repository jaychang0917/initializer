An initialization tasks manager for Android. It performs all initialization tasks in defined order.
Also, it supports late and async initialization.

# Download
Initializer is available on `mavenCentral()`.
```
implementation("io.github.jaychang0917:initializer:0.0.1")
```    

# Quick Start
1. Creates initializer(s).
```kotlin
// Initializes after InitializerB and InitializerC.
class InitializerA : Initializer() {
    override fun init(context: Context) {
        // Perform initialization here
    }

    override fun dependencies(): List<Class<out Initializer>> = listOf(
        InitializerB::class.java,
        InitializerC::class.java
    )
} 

class InitializerB : Initializer() {
    // ...
} 

class InitializerC : AsyncInitializer() { 
    // Late init, allow to finish initialization after first frame. Default false.
    override val isLate: Boolean = true
    
    // Async init, perform initialization on worker thread. Default false.
    override val isAsync: Boolean = true

    override fun init(context: Context, notifier: AsyncNotifier) {
        // Run on worker thread.
        // Perform initialization here and notify `AppInitializer` that it finished.
        notifier.finish()
    }
        
    // ...
}
```
2. Initializes them using `AppInitializer#init`.
```kotlin
val appInitializer = AppInitializer.Builder(this).build()
appInitializer.init(InitializerA(), InitializerB(), InitializerC())
```
 
# License
```
 Copyright (C) 2021. Jay Chang
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```
