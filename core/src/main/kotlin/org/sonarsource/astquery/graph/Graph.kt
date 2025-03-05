package org.sonarsource.astquery.graph

open class Graph<R : N, N : Node<N>>(val root: R) {

    val nodes: Map<Int, N> by lazy {
        GraphUtils.breathFirst(root).associateBy { it.id }
    }

    val sinks: Set<N> by lazy {
        nodes.values.filter { it.isSink }.toSet()
    }
}
