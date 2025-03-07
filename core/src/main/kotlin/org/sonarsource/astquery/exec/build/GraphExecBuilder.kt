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

import org.sonarsource.astquery.exec.Executable
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.graph.GraphUtils
import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.ir.IRGraph
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import org.sonarsource.astquery.ir.nodes.*

interface BuildCtx {

  fun addTranslation(ir: IR, translated: Node<*>)

  fun <IN> getExecutable(): Executable<IN>
}

abstract class GraphExecBuilder<CTX: BuildCtx, N : Node<N>>(
    irTransformations: List<Transformation<IR>>,
    protected val funcRegistry: NodeFunctionRegistry<CTX, N>
) : ExecBuilder(irTransformations) {

  protected abstract fun newContext(): CTX

  protected fun getNodeFunction(ctx: CTX, func: IdentifiedNodeFunction<*>, ir: IRNode<*, *>): N {
    return funcRegistry.translateNode(ctx, ir, func) ?: throw IllegalStateException("No function found for $ir and $func")
  }

  override fun <IN> translate(graph: IRGraph<IN>): Executable<IN> {
    // Traverse the tree in post-order to ensure that the children are translated before the parent
    val nodes = GraphUtils.postOrder(graph.root)
    val ctx = newContext()

    for (node in nodes) {
      val ir = node
      val translated = when (ir) {
        is Aggregate<*, *> -> translateAggregate(ir, ctx)
        is AggregateDrop<*, *> -> translateAggregateDrop(ir, ctx)
        is Combine<*, *, *> -> translateCombine(ir, ctx)
        is CombineDrop<*, *, *> -> translateCombineDrop(ir, ctx)
        is Consumer<*> -> translateConsumer(ir, ctx)
        is Filter<*> -> translateFilter(ir, ctx)
        is FilterNonNull<*> -> translateFilterNonNull(ir, ctx)
        is FilterType<*, *> -> translateFilterType(ir, ctx)
        is FlatMap<*, *> -> translateFlatMap(ir, ctx)
        is IRMap<*, *> -> translateMap(ir, ctx)
        is Root<*> -> translateRoot(ir, ctx)
        is Scope<*> -> translateScope(ir, ctx)
        is Unscope<*> -> translateUnscope(ir, ctx)
        is Union<*> -> translateUnion(ir, ctx)
      }

      ctx.addTranslation(ir, translated)
    }

    return ctx.getExecutable<IN>()
  }

  abstract fun <IN, OUT> translateAggregate(ir: Aggregate<IN, OUT>, ctx: CTX): N

  abstract fun <IN, OUT> translateAggregateDrop(ir: AggregateDrop<IN, OUT>, ctx: CTX): N

  abstract fun <LT, RT, OUT> translateCombine(ir: Combine<LT, RT, OUT>, ctx: CTX): N

  abstract fun <LT, RT, OUT> translateCombineDrop(ir: CombineDrop<LT, RT, OUT>, ctx: CTX): N

  abstract fun <IN> translateConsumer(ir: Consumer<IN>, ctx: CTX): N

  abstract fun <IN> translateFilter(ir: Filter<IN>, ctx: CTX): N

  abstract fun <IN> translateFilterNonNull(ir: FilterNonNull<IN>, ctx: CTX): N

  abstract fun <IN, OUT : IN & Any> translateFilterType(ir: FilterType<IN, OUT>, ctx: CTX): N

  abstract fun <IN, OUT> translateFlatMap(ir: FlatMap<IN, OUT>, ctx: CTX): N

  abstract fun <IN, OUT> translateMap(ir: IRMap<IN, OUT>, ctx: CTX): N

  abstract fun <IN> translateRoot(ir: Root<IN>, ctx: CTX): N

  abstract fun <IN> translateScope(ir: Scope<IN>, ctx: CTX): N

  abstract fun <IN> translateUnscope(ir: Unscope<IN>, ctx: CTX): N

  abstract fun <IN> translateUnion(ir: Union<IN>, ctx: CTX): N
}