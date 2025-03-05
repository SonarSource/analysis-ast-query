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

import org.sonarsource.astquery.exec.Builder
import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.batch.core.*
import org.sonar.plugins.java.api.query.graph.exec.batch.specialized.CountNode
import org.sonar.plugins.java.api.query.graph.exec.batch.specialized.ExistsNode
import org.sonar.plugins.java.api.query.graph.exec.batch.specialized.first.FirstNode
import org.sonar.plugins.java.api.query.graph.exec.batch.specialized.first.FirstOrDefaultNode
import org.sonar.plugins.java.api.query.graph.exec.batch.tree.FastSubtreeNode
import org.sonar.plugins.java.api.query.graph.exec.batch.tree.TreeParentsNode
import org.sonar.plugins.java.api.query.graph.ir.CountFunction
import org.sonar.plugins.java.api.query.graph.ir.ExistFunction
import org.sonar.plugins.java.api.query.graph.ir.FirstFunction
import org.sonar.plugins.java.api.query.graph.ir.FirstOrDefaultFunction
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.graph.ir.NotExistFunction
import org.sonar.plugins.java.api.query.graph.ir.SubtreeFunction
import org.sonar.plugins.java.api.query.graph.ir.TreeParentFunction
import org.sonar.plugins.java.api.query.graph.ir.nodes.*
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRMap
import org.sonar.plugins.java.api.tree.Tree

private typealias TranslatedMap = MutableMap<NodeId, BatchNode<*, *>>

private class BuildContext(
  val translated: TranslatedMap = mutableMapOf(),
)

class BatchBuilder : Builder<BatchNode<*, *>>() {

  override fun <IN> build(root: Root<IN>): BatchGraph<IN> {
    untangleScopes(root)
    applyMergeOptimization(root)

    val context = BuildContext()
    translate(root, context)

    return BatchGraph(getTranslatedNode(root, context.translated))
  }

  @Suppress("UNCHECKED_CAST")
  private fun <IN, OUT> getTranslatedNode(ir: IRNode<IN, OUT>, translated: TranslatedMap) =
    translated.getValue(ir.id) as BatchNode<IN, OUT>

  private fun <OUT> getChildren(ir: IRNode<*, OUT>, translated: TranslatedMap) =
    ir.children.map { child -> getTranslatedNode(child, translated) }

  private fun <IN, OUT> translate(ir: IRNode<IN, OUT>, context: BuildContext) {
    if (context.translated.containsKey(ir.id)) return

    ir.children.forEach { child -> translate(child, context) }

    context.translated[ir.id] = translate(ir, context.translated)
  }

  private fun <IN, OUT> translate(
    ir: IRNode<IN, OUT>,
    translated: TranslatedMap,
  ): BatchNode<*, *> {
    val node = when (ir) {
      is Aggregate<*, *> -> translateAggregate(ir, translated)
      is AggregateDrop<*, *> -> translateAggregateDrop(ir, translated)
      is Combine<*, *, *> -> translateCombine(ir, translated)
      is CombineDrop<*, *, *> -> translateCombineDrop(ir, translated)
      is Consumer<*> -> ConsumerNode(ir.id, getChildren(ir, translated), ir.consumer)
      is Filter<*> -> translateFilter(ir, translated)
      is FilterNonNull -> FilterNonNullNode(ir.id, getChildren(ir, translated))
      is FilterType<*, *> -> translateFilterType(ir, translated)
      is FlatMap -> translateFlatMap(ir, translated)
      is IRMap -> translateMap(ir, translated)
      is Root<*> -> RootNode(ir.id, getChildren(ir, translated))
      is Scope<*> -> ScopeNode(ir.id, getChildren(ir, translated), ir.scopeId)
      is UnScope<*> ->
        UnScopeNode(ir.id, getChildren(ir, translated), ir.scopeStarts.map { it.scopeId }.toSet())
      is Union<*> -> UnionNode(ir.id, getChildren(ir, translated), ir.parents.size)
    }

    return node
  }

  private fun <IN, OUT> translateAggregate(
    ir: Aggregate<IN, OUT>,
    translated: TranslatedMap,
  ): BatchNode<IN, OUT> {

    @Suppress("UNCHECKED_CAST") fun translateExist(
      ir: Aggregate<IN, Boolean>, map: TranslatedMap, inverted: Boolean
    ) = ExistsNode<IN>(ir.id, getChildren(ir, map), inverted) as BatchNode<IN, OUT>

    @Suppress("UNCHECKED_CAST") fun translateCount(
      ir: Aggregate<IN, Int>, map: TranslatedMap
    ) = CountNode<IN>(ir.id, getChildren(ir, map)) as BatchNode<IN, OUT>

    @Suppress("UNCHECKED_CAST") fun translateFirstOrDefault(
      ir: Aggregate<IN, OUT>, default: OUT, map: TranslatedMap
    ) = FirstOrDefaultNode<OUT>(ir.id, getChildren(ir, map), default) as BatchNode<IN, OUT>

    @Suppress("UNCHECKED_CAST")
    return when (ir.transform) {
      is IdentifiedLambda ->
        AggregateNode(ir.id, getChildren(ir, translated), ir.transform.function)

      is ExistFunction -> translateExist(ir as Aggregate<IN, Boolean>, translated, false)
      is NotExistFunction -> translateExist(ir as Aggregate<IN, Boolean>, translated, true)
      is CountFunction -> translateCount(ir as Aggregate<IN, Int>, translated)

      is FirstOrDefaultFunction<*> -> translateFirstOrDefault(ir, ir.transform.default as OUT, translated)

      else -> error("Unsupported function type: ${ir.transform}")
    }
  }



  private fun <IN, OUT> translateAggregateDrop(
    ir: AggregateDrop<IN, OUT>,
    translated: TranslatedMap,
  ): BatchNode<IN, OUT> {

    @Suppress("UNCHECKED_CAST") fun translateFirst(
      ir: AggregateDrop<IN, IN>, map: TranslatedMap
    ) = FirstNode<IN>(ir.id, getChildren(ir, map)) as BatchNode<IN, OUT>

    @Suppress("UNCHECKED_CAST")
    return when (ir.transform) {
      is IdentifiedLambda ->
        AggregateDropNode(ir.id, getChildren(ir, translated), ir.transform.function)

      is FirstFunction<*> -> translateFirst(ir as AggregateDrop<IN, IN>, translated)

      else -> error("Unsupported function type: ${ir.transform}")
    }
  }


  private fun <IN> translateFilter(
    ir: Filter<IN>,
    translated: TranslatedMap,
  ): FilterNode<IN> {
    return when (ir.predicate) {
      is IdentifiedLambda -> FilterNode(ir.id, getChildren(ir, translated), ir.predicate.function)
      else -> error("Unsupported function type: ${ir.predicate}")
    }
  }

  private fun <IN, OUT : IN & Any> translateFilterType(
    ir: FilterType<IN, OUT>,
    translated: TranslatedMap,
  ): FilterTypeNode<IN, OUT> {
    return FilterTypeNode(ir.id, getChildren(ir, translated), ir.types)
  }

  private fun <IN, OUT> translateFlatMap(
    ir: FlatMap<IN, OUT>,
    translated: TranslatedMap,
  ): BatchNode<IN, OUT> {
    @Suppress("UNCHECKED_CAST")
    fun translateSubtree(subtree: SubtreeFunction) =
      FastSubtreeNode(
        ir.id,
        getChildren(ir, translated) as List<ChildNode<Tree>>,
        subtree.stopAt,
        subtree.includeRoot
      ) as BatchNode<IN, OUT>

    @Suppress("UNCHECKED_CAST")
    fun translateTreeParents() =
      TreeParentsNode(
        ir.id,
        getChildren(ir, translated) as List<ChildNode<Tree>>,
      ) as BatchNode<IN, OUT>

    return when (ir.mapper) {
      is IdentifiedLambda -> FlatMapNode(ir.id, getChildren(ir, translated), ir.mapper.function)
      is SubtreeFunction -> translateSubtree(ir.mapper)
      is TreeParentFunction -> translateTreeParents()
      else -> error("Unsupported function type: ${ir.mapper}")
    }
  }

  private fun <IN, OUT> translateMap(
      ir: IRMap<IN, OUT>,
      translated: TranslatedMap,
  ): MapNode<IN, OUT> {
    return when (ir.mapper) {
      is IdentifiedLambda -> MapNode(ir.id, getChildren(ir, translated), ir.mapper.function)
      else -> error("Unsupported function type: ${ir.mapper}")
    }
  }

  private fun <OUT> translateCombine(
    ir: Combine<*, *, OUT>,
    translated: TranslatedMap,
  ): CombineNode<*, *, OUT> {
    return when (ir.combineFunc) {
      is IdentifiedLambda ->
        CombineNode(
          ir.id,
          getChildren(ir, translated),
          ir.left.id,
          ir.right.id,
          ir.combineFunc.function
        )

      else -> error("Unsupported function type: ${ir.combineFunc}")
    }
  }

  private fun <OUT> translateCombineDrop(
    ir: CombineDrop<*, *, OUT>,
    translated: TranslatedMap,
  ): CombineDropNode<*, *, OUT> {
    return when (ir.combineFunc) {
      is IdentifiedLambda ->
        CombineDropNode(
          ir.id,
          getChildren(ir, translated),
          ir.left.id,
          ir.right.id,
          ir.combineFunc.function
        )

      else -> error("Unsupported function type: ${ir.combineFunc}")
    }
  }
}
