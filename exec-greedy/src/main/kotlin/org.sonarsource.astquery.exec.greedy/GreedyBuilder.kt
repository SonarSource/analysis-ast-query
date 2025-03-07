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

package org.sonarsource.astquery.exec.greedy

import org.sonarsource.astquery.exec.Executable
import org.sonarsource.astquery.exec.build.BuildCtx
import org.sonarsource.astquery.exec.build.GraphExecBuilder
import org.sonarsource.astquery.exec.build.NodeFunctionRegistry
import org.sonarsource.astquery.exec.greedy.core.*
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.ScopeId
import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import org.sonarsource.astquery.ir.nodes.*

class GreedyBuildCtx : BuildCtx {

  val translatedMap = mutableMapOf<NodeId, GreedyNode<*, *>>()
  private var root: NodeId? = null

  override fun addTranslation(ir: IR, translated: Node<*>) {
    if (root == null && ir is Root<*>) {
      root = ir.id
    }

    translatedMap.put(ir.id, translated as GreedyNode<*, *>)
  }

  fun <IN, OUT> getChildren(ir: IRNode<IN, OUT>): List<GreedyNode<OUT, *>> {
    return ir.children.map { child -> getTranslation(child.id) }
  }

  fun <N : GreedyNode<*, *>> getTranslation(nodeId: NodeId): N {
    @Suppress("UNCHECKED_CAST")
    return translatedMap[nodeId]!! as N
  }

  override fun <IN> getExecutable(): Executable<IN> {
    val rootId = root ?: error("Root node not found")
    return GreedyGraph(getTranslation<RootNode<IN>>(rootId))
  }
}

class GreedyBuilder(
  irTransformations: List<Transformation<IR>>,
  funcRegistry: NodeFunctionRegistry<GreedyBuildCtx, GreedyNode<*, *>>
) : GraphExecBuilder<GreedyBuildCtx, GreedyNode<*, *>>(irTransformations, funcRegistry) {

  override fun newContext(): GreedyBuildCtx {
    return GreedyBuildCtx()
  }

  private fun getCommonScopes(node1: IRNode<*, *>, node2: IRNode<*, *>): Set<ScopeId> {
    return node1.ancestors.filterIsInstance<Scope<*>>()
      .intersect(node2.ancestors.filterIsInstance<Scope<*>>())
      .map(Scope<*>::scopeId)
      .toSet() + ROOT_SCOPE_ID
  }

  override fun <IN, OUT> translateAggregate(
    ir: Aggregate<IN, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.transform
    return when (transform) {
      is IdentifiedLambda -> AggregateNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN, OUT> translateAggregateDrop(
    ir: AggregateDrop<IN, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.transform
    return when (transform) {
      is IdentifiedLambda -> AggregateDropNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <LT, RT, OUT> translateCombine(
    ir: Combine<LT, RT, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.combineFunc
    val commonScopes = getCommonScopes(ir.left, ir.right)
    return when (transform) {
      is IdentifiedLambda ->
        CombineNode(ir.id, ctx.getChildren(ir), ir.left.id, ir.right.id, commonScopes, transform.function)
      is IdentifiedNodeFunction ->
        getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <LT, RT, OUT> translateCombineDrop(
    ir: CombineDrop<LT, RT, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.combineFunc
    val commonScopes = getCommonScopes(ir.left, ir.right)
    return when (transform) {
      is IdentifiedLambda ->
        CombineDropNode(ir.id, ctx.getChildren(ir), ir.left.id, ir.right.id, commonScopes, transform.function)
      is IdentifiedNodeFunction ->
        getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN> translateConsumer(
    ir: Consumer<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    return ConsumerNode(ir.id, ctx.getChildren(ir), ir.consumer)
  }

  override fun <IN> translateFilter(
    ir: Filter<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.predicate
    return when (transform) {
      is IdentifiedLambda -> FilterNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN> translateFilterNonNull(
    ir: FilterNonNull<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    return FilterNonNullNode(ir.id, ctx.getChildren(ir))
  }

  override fun <IN, OUT : IN & Any> translateFilterType(
    ir: FilterType<IN, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    return FilterTypeNode(ir.id, ctx.getChildren(ir), ir.types)
  }

  override fun <IN, OUT> translateFlatMap(
    ir: FlatMap<IN, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.mapper
    return when (transform) {
      is IdentifiedLambda -> FlatMapNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN, OUT> translateMap(
    ir: IRMap<IN, OUT>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val transform = ir.mapper
    return when (transform) {
      is IdentifiedLambda -> MapNode(ir.id, ctx.getChildren(ir), transform.function)
      is IdentifiedNodeFunction -> getNodeFunction(ctx, transform, ir)
    }
  }

  override fun <IN> translateRoot(
    ir: Root<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    return RootNode(ir.id, ctx.getChildren(ir))
  }

  override fun <IN> translateScope(
    ir: Scope<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    return ScopeNode(ir.id, ctx.getChildren(ir), ir.scopeId)
  }

  override fun <IN> translateUnscope(
    ir: Unscope<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    val scopeIds = ir.scopeStarts.map { it.scopeId }.toSet()
    return UnScopeNode(ir.id, ctx.getChildren(ir), scopeIds)
  }

  override fun <IN> translateUnion(
    ir: Union<IN>,
    ctx: GreedyBuildCtx
  ): GreedyNode<*, *> {
    if (ir.parents.size != 2) {
      error("Union node must have exactly 2 parents")
    }

    val (left, right) = ir.parents.toList()
    val commonScopes = getCommonScopes(left, right)
    return UnionNode(ir.id, ctx.getChildren(ir), left.id, right.id, commonScopes)
  }
}