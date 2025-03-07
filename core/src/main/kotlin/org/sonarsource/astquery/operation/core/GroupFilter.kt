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
package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.nodes.*
import org.sonarsource.astquery.operation.Droppable
import org.sonarsource.astquery.operation.Droppable.Drop
import org.sonarsource.astquery.operation.Droppable.Keep
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.composite.orElse

class GroupFilterWithScopeOperation <FROM, GROUPED, TO>(
  val groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  val grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>,
) : Operation1toOptional<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    val scope = Scope(parent)
    val group = groupProducer(SingleBuilder(scope)).toSingle()
    val combine = CombineDrop(scope, group.irNode, grouping)
    return Unscope(combine, setOf(scope))
  }
}

fun <FROM, GROUPED, TO> SingleBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): OptionalBuilder<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> OptionalBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): OptionalBuilder<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> ManyBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): ManyBuilder<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

private fun <A, B> dropPairFunction() = IdentifiedLambda("dropPair", "DroppablePair") { from: A, drop: Droppable<B> ->
  if (drop is Keep) Keep(Pair(from, drop.data)) else Drop
}

private fun <T> keepFunction() = IdentifiedLambda("keep", "Keep") { value: T -> Keep(value) }
private fun <FROM, TO> toDroppable(func: (SingleBuilder<FROM>) -> OptionalBuilder<TO>) =
  { from: SingleBuilder<FROM> -> func(from).map(keepFunction()).orElse(Drop) }

fun <FROM, TO> SingleBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> OptionalBuilder<TO>
): OptionalBuilder<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

fun <FROM, TO> OptionalBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> OptionalBuilder<TO>
): OptionalBuilder<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

fun <FROM, TO> ManyBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> OptionalBuilder<TO>
): ManyBuilder<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

private fun <T> whereFunction() = IdentifiedLambda<(T, Boolean) -> Droppable<T>>("where", "Where") { value, keep ->
  if (keep) Keep(value) else Drop
}

fun <FROM> SingleBuilder<FROM>.where(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, Boolean>
): OptionalBuilder<FROM> = groupFilterWith(groupProducer, whereFunction())

fun <FROM> OptionalBuilder<FROM>.where(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, Boolean>
): OptionalBuilder<FROM> = groupFilterWith(groupProducer, whereFunction())

fun <FROM> ManyBuilder<FROM>.where(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, Boolean>
): ManyBuilder<FROM> = groupFilterWith(groupProducer, whereFunction())
