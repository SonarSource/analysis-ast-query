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