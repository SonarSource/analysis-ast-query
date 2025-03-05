package org.sonarsource.astquery.graph

import org.jetbrains.kotlin.fir.resolve.dfa.isNotEmpty
import org.jetbrains.kotlin.fir.resolve.dfa.stackOf
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.Root

object GraphUtils {

  fun <N : Node<N>> postOrder(start: N): Sequence<N> {
    // Pair of node and flag if children were visited
    val stack = stackOf(start to false)

    return sequence {
      while (stack.isNotEmpty) {
        val (currentNode, childrenVisited) = stack.pop()

        if (childrenVisited) {
          yield(currentNode)
        } else {
          // Mark children as visited to yield the node later
          stack.push(currentNode to true)

          for (child in currentNode.children) {
            stack.push(child to false)
          }
        }
      }
    }
  }

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
