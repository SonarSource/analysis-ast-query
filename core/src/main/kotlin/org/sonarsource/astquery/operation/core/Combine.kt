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

import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.nodes.Combine
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.operation.Operation1to1
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.idFunction

class CombineOperation<LEFT, RIGHT, OUT>(
    private val combined: SingleBuilder<RIGHT>,
    private val combinator: IdentifiedFunction<(LEFT, RIGHT) -> OUT>,
) : Operation1to1<LEFT, OUT> {

  override fun applyTo(parent: ParentNode<LEFT>): IRNode<*, out OUT> {
    return Combine(parent, combined.irNode, combinator)
  }
}

fun <LEFT, RIGHT, OUT> SingleBuilder<LEFT>.combine(
    other: SingleBuilder<RIGHT>,
    combiner: IdentifiedFunction<(LEFT, RIGHT) -> OUT>,
): SingleBuilder<OUT> {
  return apply(CombineOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> OptionalBuilder<LEFT>.combine(
    other: SingleBuilder<RIGHT>,
    combiner: IdentifiedFunction<(LEFT, RIGHT) -> OUT>,
): OptionalBuilder<OUT> {
  return apply(CombineOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> ManyBuilder<LEFT>.combine(
    other: SingleBuilder<RIGHT>,
    combiner: IdentifiedFunction<(LEFT, RIGHT) -> OUT>,
): ManyBuilder<OUT> {
  return apply(CombineOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> SingleBuilder<LEFT>.combine(
    other: SingleBuilder<RIGHT>,
    combiner: (LEFT, RIGHT) -> OUT
) = combine(other, idFunction(lambda = combiner))

fun <LEFT, RIGHT, OUT> OptionalBuilder<LEFT>.combine(
    other: SingleBuilder<RIGHT>,
    combiner: (LEFT, RIGHT) -> OUT
) = combine(other, idFunction(lambda = combiner))

fun <LEFT, RIGHT, OUT> ManyBuilder<LEFT>.combine(
    other: SingleBuilder<RIGHT>,
    combiner: (LEFT, RIGHT) -> OUT
) = combine(other, idFunction(lambda = combiner))
