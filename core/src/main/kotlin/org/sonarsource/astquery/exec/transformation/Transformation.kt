package org.sonarsource.astquery.exec.transformation

import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.graph.Node

interface Transformation<N: Node<N>> {
    fun <R : N> apply(input: Graph<R, N>): Graph<R, N>
}