package org.sonar.plugins.java.api.query.graph

import org.sonarsource.astquery.graph.TranslationTable
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.Root

object GraphUtils {

  fun <N : Node<N>> topologicalSort(start: N): List<N> {
    val visited = mutableSetOf<N>()
    val result = mutableListOf<N>()

    fun dfs(node: N) {
      visited.add(node)
      for (child in node.children) {
        if (child !in visited) {
          dfs(child)
        }
      }
      result.add(node)
    }

    dfs(start)

    return result.reversed()
  }

  fun <N : Node<N>> breathFirst(start: N): Sequence<N> {
    val visited = mutableSetOf<N>()
    val queue = ArrayDeque<N>()

    queue.add(start)
    visited.add(start)

    return sequence {
      while (queue.isNotEmpty()) {
        val current = queue.removeFirst()

        yield(current)

        for (child in current.children) {
          if (child !in visited) {
            queue.add(child)
            visited.add(child)
          }
        }
      }
    }
  }

  fun <OUT> copySubTree(from: IRNode<*, out OUT>, to: IRNode<*, out OUT>, subTreeTo: IRNode<*, *>) {
    val toCopy = topologicalSort(from)
      .drop(1) // Skip from
      .filter { it in subTreeTo.strictAncestors } // Only copy nodes that are ancestors of subTreeTo

    val table = TranslationTable()
    table.addParent(from, to)
    copyNodes(toCopy, table)

    subTreeTo.applyTranslation(table)
  }

  fun removeDeadBranches(root: Root<*>) {
    breathFirst(root)
      .filter { !it.hasSink }
      .toList()
      .forEach { it.delete() }
  }

  fun <T> removeNodeAndStitch(node: IRNode<T, T>) {
    val parents = node.parents
    require(parents.size == 1) { "For stitch to work, the node must have exactly one parent" }
    val parent = parents.single()

    val table = TranslationTable()
    table.addParent(node, parent)
    val children = node.children

    children.forEach { it.applyTranslation(table) }

    node.delete()
  }

  fun copyNodes(toCopy: List<IRNode<*, *>>, initialTable: TranslationTable = TranslationTable()): TranslationTable {
    val newParents = initialTable

    toCopy.forEach { node ->
      val copy = node.copy()
      newParents.addParent(node, copy)
    }

    toCopy.forEach { node ->
      newParents.get(node).applyTranslation(newParents)
    }

    return newParents
  }

  fun <IN> copyTree(root: Root<IN>): TranslationTable {
    val toCopy: List<IRNode<*, *>> = breathFirst(root).toList()
    return copyNodes(toCopy)
  }
}
