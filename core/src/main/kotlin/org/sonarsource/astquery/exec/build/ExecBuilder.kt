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
package org.sonarsource.astquery.exec.build

import org.sonarsource.astquery.Query
import org.sonarsource.astquery.exec.Executable
import org.sonarsource.astquery.exec.Store
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.graph.GraphUtils
import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.ir.IRGraph
import org.sonarsource.astquery.ir.nodes.Consumer
import org.sonarsource.astquery.ir.nodes.Root
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder

abstract class ExecBuilder(
  private val irTransformations: List<Transformation<IR>>
) {

  fun <IN> build(root: Root<IN>): Executable<IN> {
    val optimizedIR = irTransformations.fold(Graph(root)) { acc, transformation ->
      transformation.apply(acc)
    }

    return translate(optimizedIR)
  }

  abstract fun <IN> translate(graph: IRGraph<IN>): Executable<IN>

  fun <INPUT, OUTPUT> createQuery(operations: (SingleBuilder<INPUT>) -> PipelineBuilder<*, OUTPUT>): Query<INPUT, OUTPUT> {
    val root = Root<INPUT>()
    val selector = SingleBuilder(root)
    val output = operations(selector)

    return buildQuery(root, output)
  }

  private fun <IN, END, OUT> buildQuery(root: Root<IN>, end: PipelineBuilder<END, OUT>): Query<IN, OUT> {

    val table = GraphUtils.copyTree(root)
    val root = table.get(root) as Root<IN>
    val endNode = table.get(end.irNode)

    val collector = Store { mutableListOf<END>() }
    // Create the Consumer node that will collect the results
      Consumer(endNode) { ctx, value ->
          collector.get(ctx).add(value)
      }

    val graph = build(root)

    return Query { ctx, input ->
        graph.execute(ctx, input)

        collector.get(ctx).let(end::toOutput)
    }
  }
}