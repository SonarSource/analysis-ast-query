package org.sonarsource.astquery.exec.transformation

import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.ir.nodes.ChildNode
import org.sonarsource.astquery.ir.nodes.IRNode

class MergeEquivalentChildren: Transformation<IR> {

    override fun <R : IR> apply(input: Graph<R, IR>): Graph<R, IR> {
        applyMergeOptimization(input.root)
        return Graph(input.root)
    }

    private fun <T> applyMergeOptimization(node: IRNode<*, T>) {
        fun <OUT> tryToMerge(index: Int, child: IRNode<*, OUT>): Boolean {
            val candidates = node.children
                .filterIndexed { i, other -> i > index && other.canMergeWith(child) }
                .toList()

            candidates.forEach { candidate ->
                @Suppress("UNCHECKED_CAST")
                candidate.children.forEach { c ->
                    child.addChild(c as ChildNode<OUT>)
                }

                candidate.delete()
            }

            // Return true if any candidates were merged
            return candidates.isNotEmpty()
        }

        fun <T> mergeEquivalentChildren(node: IRNode<*, T>) {
            while (true) {
                var merged = false
                for (i in node.children.indices) {
                    if (tryToMerge(i, node.children[i])) {
                        merged = true
                        break
                    }
                }

                if (!merged) {
                    return
                }
            }
        }

        mergeEquivalentChildren(node)
        node.children.forEach { applyMergeOptimization(it) }
    }
}