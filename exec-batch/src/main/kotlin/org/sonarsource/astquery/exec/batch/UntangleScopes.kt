package org.sonarsource.astquery.exec.batch

import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.graph.GraphUtils
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.Unscope

class UntangleScopes: Transformation<IR> {

    override fun <R : IR> apply(input: Graph<R, IR>): Graph<R, IR> {
        untangleScopes(input.root)
        return Graph(input.root)
    }

    private data class TangledScope(val mainScope: Scope<*>, val mainUnscope: Unscope<*>, val tangledScope: Scope<*>)

    private fun untangleScopes(input: IR) {
        while (true) {
            val nextTangledScope = findTangledScope(input) ?: return

            GraphUtils.removeNodeAndStitch(nextTangledScope.mainUnscope)
            nextTangledScope.tangledScope.unscopes.forEach {
                it.addScopeStart(nextTangledScope.mainScope)
            }
        }
    }

    private fun findTangledScope(input: IR): TangledScope? {
        val scopeCandidates = GraphUtils.topologicalSort(input)
            .filterIsInstance<Scope<*>>()

        for (candidate in scopeCandidates) {
            for (unscope in candidate.unscopes) {
                val scopedNodes = candidate.strictDescendants
                    .intersect(unscope.strictAncestors)

                // A tangled scope is any scoped scope that has an unscoped node that is outside the current scope
                val tangledScope = scopedNodes
                    .filterIsInstance<Scope<*>>()
                    .firstOrNull { s -> s.unscopes.any { it !in scopedNodes } }

                if (tangledScope != null) {
                    return TangledScope(candidate, unscope, tangledScope)
                }
            }
        }

        return null
    }
}