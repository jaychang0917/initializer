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
import java.lang.IllegalStateException
import java.util.*

/**
 * Directed acyclic graph links all the [Initializer]s to form before/after relationships.
 *
 * Validations are performed against the graph:
 * - No sync initializer can depend on sync-late initializer(s).
 * - Circular dependencies.
 * */
internal class AppInitializerGraph(initializers: List<Initializer>) {
    private val root = Root()
    val vertexes = initializers + root
    private val initializersMap = buildInitializersMap(vertexes)
    private val adjacencyList = mutableMapOf<Initializer, MutableList<Initializer>>()

    private fun buildInitializersMap(initializers: List<Initializer>): Map<Class<out Initializer>, Initializer> {
        val map = mutableMapOf<Class<out Initializer>, Initializer>()
        for (initializer in initializers) {
            map[initializer.javaClass] = initializer
        }
        return map
    }

    init {
        buildGraph()
        validateGraph()
    }

    private fun buildGraph() {
        for (vertex in vertexes) {
            addVertex(vertex)
        }

        for (vertex in vertexes) {
            val dependencies = vertex.dependencies().map { initializersMap[it]!! }.toList()
            for (before in dependencies) {
                before.addEdge(vertex)
            }
            // Link all vertexes having no dependencies to root as we need to start traversing the graph
            // from the root.
            if (dependencies.isEmpty() && vertex !is Root) {
                root.addEdge(vertex)
            }
        }
    }

    private fun addVertex(vertex: Initializer) {
        adjacencyList[vertex] = mutableListOf()
    }

    private fun Initializer.addEdge(to: Initializer) {
        adjacencyList[this]!!.add(to)
        addAfter(to)
        to.addBefore(this)
    }

    private fun validateGraph() {
        traversal(root) { it.checkSyncDependOnSyncLate() }
    }

    // Sync initializer can't depend on Sync-late initializers because we must wait for
    // all sync initializers to be finished before executing sync-late ones.
    private fun Initializer.checkSyncDependOnSyncLate() {
        if (!isAsync) {
            for (before in beforeInitializers) {
                val hasSyncLateDependencies = !before.isAsync && before.isLate
                if (hasSyncLateDependencies) {
                    val error = "Sync initializer (${javaClass.simpleName}) can't depend on Sync-late initializer (${before.javaClass.simpleName})"
                    throw IllegalStateException(error)
                }
            }
        }
    }

    @Suppress("UnusedPrivateMember")
    // TODO dfs using single stack can't check cycle.
    private fun Initializer.checkCycle(path: MutableList<Initializer>) {
        if (this in path) {
            val pathString = path.joinToString("->")
            val error = "Circular dependency detected. Path: [$pathString]"
            throw IllegalStateException(error)
        } else {
            path.add(this)
        }
    }

    fun start() {
        root.start()
    }

    // Traversal the graph using DFS algorithm.
    private fun traversal(vertex: Initializer, visitor: ((Initializer) -> Unit)? = null) {
        val stack = Stack<Initializer>()
        val visited = mutableSetOf<Initializer>()
        stack.push(vertex)

        while (stack.isNotEmpty()) {
            val current = stack.pop()
            visitor?.invoke(current)
            visited.add(current)
            val neighbors = adjacencyList[current] ?: continue
            for (neighbor in neighbors) {
                if (!visited.contains(neighbor)) {
                    stack.add(neighbor)
                }
            }
        }
    }

    class Root : Initializer() {
        override fun init(context: android.content.Context) = Unit
        override fun dependencies(): List<Class<out Initializer>> = emptyList()
    }
}
