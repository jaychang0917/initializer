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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import java.lang.IllegalStateException
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AppInitializerTest {
    private val appContext = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `single initializer`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A : TestInitializer()
        val A = spyk(_A())

        appInitializer.init(A)

        verify { A.init(appContext) }
    }

    /**
     *       A1  A2
     *       \  /
     *        B
     * */
    @Test
    fun `multiple before, single after`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A1 : TestInitializer()
        class _A2 : TestInitializer()
        class _B : TestInitializer()
        val A1 = spyk(_A1())
        val A2 = spyk(_A2())
        val B = spyk(_B())
        B.dependencies = listOf(A1, A2)

        appInitializer.init(A1, A2, B)

        verifyOrder {
            A1.init(appContext)
            A2.init(appContext)
            B.init(appContext)
        }
    }

    /**
     *         A
     *        / \
     *       B1 B2
     * */
    @Test
    fun `single before, multiple after`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A : TestInitializer()
        class _B1 : TestInitializer()
        class _B2 : TestInitializer()
        val A = spyk(_A())
        val B1 = spyk(_B1())
        val B2 = spyk(_B2())
        B1.dependencies = listOf(A)
        B2.dependencies = listOf(A)

        appInitializer.init(A, B1, B2)

        verifyOrder {
            A.init(appContext)
            B1.init(appContext)
            B2.init(appContext)
        }
    }

    /**
     *         A  C
     *        / \ /
     *       B1 B2
     * */
    @Test
    fun `multiple before, multiple after`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A : TestInitializer()
        class _C : TestInitializer()
        class _B1 : TestInitializer()
        class _B2 : TestInitializer()
        val A = spyk(_A())
        val C = spyk(_C())
        val B1 = spyk(_B1())
        val B2 = spyk(_B2())
        B1.dependencies = listOf(A)
        B2.dependencies = listOf(A, C)

        appInitializer.init(A, C, B1, B2)

        verifyOrder {
            A.init(appContext)
            B1.init(appContext)
            C.init(appContext)
            B2.init(appContext)
        }
    }

    /**
     *         A     C
     *        /     /
     *       B     D
     * */
    @Test
    fun `disconnected graphs`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A : TestInitializer()
        class _B : TestInitializer()
        class _C : TestInitializer()
        class _D : TestInitializer()
        val A = spyk(_A())
        val B = spyk(_B())
        val C = spyk(_C())
        val D = spyk(_D())
        B.dependencies = listOf(A)
        D.dependencies = listOf(C)

        appInitializer.init(A, B, C, D)

        verifyOrder {
            A.init(appContext)
            B.init(appContext)
            C.init(appContext)
            D.init(appContext)
        }
    }

    /**
     *     A  B(async)  C  D
     * */
    @Test
    fun async() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        val B_Latch = CountDownLatch(1)
        class _A : TestInitializer()
        class _B : AsyncTestInitializer()
        class _C : TestInitializer()
        class _D : TestInitializer()
        val A = spyk(_A())
        val B = spyk(_B())
        val C = spyk(_C())
        val D = spyk(_D())
        B.initBlock = { B_Latch.await() }
        D.initBlock = { B_Latch.countDown() }

        appInitializer.init(A, B, C, D)

        verifyOrder {
            A.init(appContext)
            C.init(appContext)
            D.init(appContext)
            B.init(appContext)
        }
    }

    /**
     *     A(depends on C)  B  C(async)
     * */
    @Test
    fun `sync depends on async`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        val C_Latch = CountDownLatch(1)
        class _C : AsyncTestInitializer()
        class _A : TestInitializer()
        class _B : TestInitializer()
        val C = spyk(_C())
        val A = spyk(_A())
        val B = spyk(_B())
        C.initBlock = { C_Latch.await() }
        A.dependencies = listOf(C)
        B.initBlock = { C_Latch.countDown() }

        appInitializer.init(A, B, C)

        verifyOrder {
            B.init(appContext)
            C.init(appContext)
            A.init(appContext)
        }
    }

    /**
     *     A  B  C(async, depends on A)
     * */
    @Test
    fun `async depends on sync`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        val C_Latch = CountDownLatch(1)
        class _A : TestInitializer()
        class _C : AsyncTestInitializer()
        class _B : TestInitializer()
        val A = spyk(_A())
        val C = spyk(_C())
        val B = spyk(_B())
        C.dependencies = listOf(A)
        C.initBlock = { C_Latch.await() }
        B.initBlock = { C_Latch.countDown() }

        appInitializer.init(A, B, C)

        verifyOrder {
            A.init(appContext)
            B.init(appContext)
            C.init(appContext)
        }
    }

    /**
     *     A(late)  B(depends on A)
     * */
    @Test
    fun `sync should not depend on sync late`() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A : TestInitializer()
        class _B : TestInitializer()
        val A = spyk(_A())
        val B = spyk(_B())
        A.isLate = true
        B.dependencies = listOf(A)

        assertThrows<IllegalStateException> {
            appInitializer.init(A, B)
        }
    }

    /**
     *     A -> B -> C -> A
     * */
    @Ignore("TODO")
    @Test
    fun cycle() {
        val appInitializer = AppInitializer.Builder(appContext).build()
        class _A : TestInitializer()
        class _B : TestInitializer()
        class _C : TestInitializer()
        val A = spyk(_A())
        val B = spyk(_B())
        val C = spyk(_C())
        B.dependencies = listOf(A)
        C.dependencies = listOf(B)
        A.dependencies = listOf(C)

        assertThrows<IllegalStateException> {
            appInitializer.init(A, B, C)
        }
    }

    private open class TestInitializer(
        var dependencies: List<Initializer> = emptyList(),
        var initBlock: (() -> Unit)? = null,
        override var isLate: Boolean = false
    ) : Initializer() {
        override fun init(context: Context) {
            initBlock?.invoke()
        }

        override fun dependencies(): List<Class<out Initializer>> {
            return dependencies.map { it::class.java }.toList()
        }
    }

    private open class AsyncTestInitializer(
        var dependencies: List<Initializer> = emptyList(),
        var initBlock: (() -> Unit)? = null,
        override var isLate: Boolean = false
    ) : AsyncInitializer() {
        override fun init(context: Context) {
            initBlock?.invoke()
        }

        override fun dependencies(): List<Class<out Initializer>> {
            return dependencies.map { it::class.java }.toList()
        }
    }
}
