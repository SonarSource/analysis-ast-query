/*
 * Copyright (C) 2018-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.astquery.exec.transformation

import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.ir.IR
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