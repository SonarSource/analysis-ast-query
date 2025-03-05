package org.sonarsource.astquery.exec.transformation

import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.graph.GraphUtils.breathFirst

class RemoveUnusedNodes: Transformation<IR> {

    override fun <R : IR> apply(input: Graph<R, IR>): Graph<R, IR> {
        breathFirst(input.root)
            .filter { !it.hasSink }
            .toList()
            .forEach { it.delete() }

        return Graph(input.root)
    }
}