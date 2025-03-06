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

package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.operation.Droppable
import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.nodes.CombineDrop
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.idFunction

class CombineFilterOperation<LEFT, RIGHT, OUT>(
    private val combined: SingleBuilder<RIGHT>,
    private val combinator: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
) : Operation1toOptional<LEFT, OUT> {

  override fun applyTo(parent: ParentNode<LEFT>): IRNode<*, out OUT> {
    return CombineDrop(parent, combined.irNode, combinator)
  }
}

fun <LEFT, RIGHT, OUT> SingleBuilder<LEFT>.combineFilter(
    other: SingleBuilder<RIGHT>,
    combiner: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
): OptionalBuilder<OUT> {
  return apply(CombineFilterOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> OptionalBuilder<LEFT>.combineFilter(
    other: SingleBuilder<RIGHT>,
    combiner: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
): OptionalBuilder<OUT> {
  return apply(CombineFilterOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> ManyBuilder<LEFT>.combineFilter(
    other: SingleBuilder<RIGHT>,
    combiner: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
): ManyBuilder<OUT> {
  return apply(CombineFilterOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> SingleBuilder<LEFT>.combineFilter(
    other: SingleBuilder<RIGHT>,
    combiner: (LEFT, RIGHT) -> Droppable<OUT>
) = combineFilter(other, idFunction(lambda = combiner))

fun <LEFT, RIGHT, OUT> OptionalBuilder<LEFT>.combineFilter(
    other: SingleBuilder<RIGHT>,
    combiner: (LEFT, RIGHT) -> Droppable<OUT>
) = combineFilter(other, idFunction(lambda = combiner))

fun <LEFT, RIGHT, OUT> ManyBuilder<LEFT>.combineFilter(
    other: SingleBuilder<RIGHT>,
    combiner: (LEFT, RIGHT) -> Droppable<OUT>
) = combineFilter(other, idFunction(lambda = combiner))
