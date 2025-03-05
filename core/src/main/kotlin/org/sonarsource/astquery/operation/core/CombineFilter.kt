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

package org.sonar.plugins.java.api.query.operation.core

import org.sonarsource.astquery.operation.Droppable
import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedFunction
import org.sonar.plugins.java.api.query.graph.ir.nodes.CombineDrop
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.ParentNode
import org.sonar.plugins.java.api.query.operation.Operation1toOptional
import org.sonar.plugins.java.api.query.operation.idFunction

class CombineFilterOperation<LEFT, RIGHT, OUT>(
  private val combined: SingleSelector<RIGHT>,
  private val combinator: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
) : Operation1toOptional<LEFT, OUT> {

  override fun applyTo(parent: ParentNode<LEFT>): IRNode<*, out OUT> {
    return CombineDrop(parent, combined.current, combinator)
  }
}

fun <LEFT, RIGHT, OUT> SingleSelector<LEFT>.combineFilter(
  other: SingleSelector<RIGHT>,
  combiner: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
): OptionalSelector<OUT> {
  return apply(CombineFilterOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> OptionalSelector<LEFT>.combineFilter(
  other: SingleSelector<RIGHT>,
  combiner: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
): OptionalSelector<OUT> {
  return apply(CombineFilterOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> ManySelector<LEFT>.combineFilter(
  other: SingleSelector<RIGHT>,
  combiner: IdentifiedFunction<(LEFT, RIGHT) -> Droppable<OUT>>,
): ManySelector<OUT> {
  return apply(CombineFilterOperation(other.toSingle(), combiner))
}

fun <LEFT, RIGHT, OUT> SingleSelector<LEFT>.combineFilter(
  other: SingleSelector<RIGHT>,
  combiner: (LEFT, RIGHT) -> Droppable<OUT>
) = combineFilter(other, idFunction(lambda = combiner))

fun <LEFT, RIGHT, OUT> OptionalSelector<LEFT>.combineFilter(
  other: SingleSelector<RIGHT>,
  combiner: (LEFT, RIGHT) -> Droppable<OUT>
) = combineFilter(other, idFunction(lambda = combiner))

fun <LEFT, RIGHT, OUT> ManySelector<LEFT>.combineFilter(
  other: SingleSelector<RIGHT>,
  combiner: (LEFT, RIGHT) -> Droppable<OUT>
) = combineFilter(other, idFunction(lambda = combiner))
