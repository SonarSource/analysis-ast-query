package org.sonar.plugins.java.api.query.graph

open class Graph<N : Node<N>>(root: N) {
  val nodes: Map<Int, N> = GraphUtils.breathFirst(root).associateBy { it.id }
  val sinks: Set<N> = nodes.values.filter { it.isSink }.toSet()
}
