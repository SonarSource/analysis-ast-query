/*
 * SonarQube Java
 * Copyright (C) 2012-2024 SonarSource SA
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

package org.sonarsource.astquery.exec.batch

import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.exec.batch.core.*
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.nodes.IRMap
import org.sonarsource.astquery.exec.Executable
import org.sonarsource.astquery.exec.build.BuildCtx
import org.sonarsource.astquery.exec.build.GraphExecBuilder
import org.sonarsource.astquery.exec.build.NodeFunctionRegistry
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import org.sonarsource.astquery.ir.nodes.Aggregate
import org.sonarsource.astquery.ir.nodes.AggregateDrop
import org.sonarsource.astquery.ir.nodes.Combine
import org.sonarsource.astquery.ir.nodes.CombineDrop
import org.sonarsource.astquery.ir.nodes.Consumer
import org.sonarsource.astquery.ir.nodes.Filter
import org.sonarsource.astquery.ir.nodes.FilterNonNull
import org.sonarsource.astquery.ir.nodes.FilterType
import org.sonarsource.astquery.ir.nodes.FlatMap
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.Root
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.Unscope
import org.sonarsource.astquery.ir.nodes.Union


class BatchBuildCtx : BuildCtx {

  val translatedMap = mutableMapOf<NodeId, BatchNode<*, *>>()
  private var root: NodeId? = null

  override fun addTranslation(ir: IR, translated: Node<*>) {
    if (root == null && ir is Root<*>) {
      root = ir.id
    }

    translatedMap.put(ir.id, translated as BatchNode<*, *>)
  }

  fun <IN, OUT> getChildren(ir: IRNode<IN, OUT>): List<BatchNode<OUT, *>> {
    return ir.children.map { child -> getTranslation(child.id) }
  }

  fun <N : BatchNode<*, *>> getTranslation(nodeId: NodeId): N {
    @Suppress("UNCHECKED_CAST")
    return translatedMap[nodeId]!! as N
  }

  override fun <IN> getExecutable(): Executable<IN> {
    val rootId = root ?: error("Root node not found")
    return BatchGraph(getTranslation<RootNode<IN>>(rootId))
  }
}

class BatchBuilder(
    irTransformations: List<Transformation<IR>>,
    funcRegistry: NodeFunctionRegistry<BatchBuildCtx, BatchNode<*, *>>
) : GraphExecBuilder<BatchBuildCtx, BatchNode<*, *>>(irTransformations, funcRegistry) {

  override fun newContext(): BatchBuildCtx {
    return BatchBuildCtx()
  }

  override fun <IN, OUT> translateAggregate(
    ir: Aggregate<IN, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.transform
    return when (transform) {
      is IdentifiedLambda -> AggregateNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN, OUT> translateAggregateDrop(
    ir: AggregateDrop<IN, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.transform
    return when (transform) {
      is IdentifiedLambda -> AggregateDropNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <LT, RT, OUT> translateCombine(
    ir: Combine<LT, RT, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.combineFunc
    return when (transform) {
      is IdentifiedLambda ->
        CombineNode(ir.id, ctx.getChildren(ir), ir.left.id, ir.right.id, transform.function)
      is IdentifiedNodeFunction ->
        getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <LT, RT, OUT> translateCombineDrop(
    ir: CombineDrop<LT, RT, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.combineFunc
    return when (transform) {
      is IdentifiedLambda ->
        CombineDropNode(ir.id, ctx.getChildren(ir), ir.left.id, ir.right.id, transform.function)
      is IdentifiedNodeFunction ->
        getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN> translateConsumer(
    ir: Consumer<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    return ConsumerNode(ir.id, ctx.getChildren(ir), ir.consumer)
  }

  override fun <IN> translateFilter(
    ir: Filter<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.predicate
    return when (transform) {
      is IdentifiedLambda -> FilterNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN> translateFilterNonNull(
    ir: FilterNonNull<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    return FilterNonNullNode(ir.id, ctx.getChildren(ir))
  }

  override fun <IN, OUT : IN & Any> translateFilterType(
    ir: FilterType<IN, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    return FilterTypeNode(ir.id, ctx.getChildren(ir), ir.types)
  }

  override fun <IN, OUT> translateFlatMap(
    ir: FlatMap<IN, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.mapper
    return when (transform) {
      is IdentifiedLambda -> FlatMapNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN, OUT> translateMap(
    ir: IRMap<IN, OUT>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val transform = ir.mapper
    return when (transform) {
      is IdentifiedLambda -> MapNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN> translateRoot(
    ir: Root<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    return RootNode(ir.id, ctx.getChildren(ir))
  }

  override fun <IN> translateScope(
    ir: Scope<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    return ScopeNode(ir.id, ctx.getChildren(ir), ir.scopeId)
  }

  override fun <IN> translateUnscope(
    ir: Unscope<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    val scopeIds = ir.scopeStarts.map { it.id }.toSet()
    return UnScopeNode(ir.id, ctx.getChildren(ir), scopeIds)
  }

  override fun <IN> translateUnion(
    ir: Union<IN>,
    ctx: BatchBuildCtx
  ): BatchNode<*, *> {
    return UnionNode(ir.id, ctx.getChildren(ir), ir.parents.size)
  }
}
