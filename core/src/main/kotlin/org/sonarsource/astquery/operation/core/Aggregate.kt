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

import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.ir.*
import org.sonarsource.astquery.ir.nodes.Aggregate
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.operation.OperationNto1
import org.sonarsource.astquery.operation.idFunction

class AggregateOperation<FROM, TO>(
  private val mapper: IdentifiedFunction<(List<FROM>) -> TO>
) : OperationNto1<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, TO> {
    return Aggregate(parent, mapper)
  }
}

fun <IN, OUT> OptionalSelector<IN>.aggregate(aggregator: IdentifiedFunction<(List<IN>) -> OUT>): SingleSelector<OUT> {
  return apply(AggregateOperation(aggregator))
}

fun <IN, OUT> ManySelector<IN>.aggregate(aggregator: IdentifiedFunction<(List<IN>) -> OUT>): SingleSelector<OUT> {
  return apply(AggregateOperation(aggregator))
}


fun <IN, OUT> OptionalSelector<IN>.aggregate(aggregator: (List<IN>) -> OUT) =
  aggregate(idFunction(lambda = aggregator))
fun <IN, OUT> ManySelector<IN>.aggregate(aggregator: (List<IN>) -> OUT) =
  aggregate(idFunction(lambda = aggregator))

fun <IN> ManySelector<IN>.aggregate(): SingleSelector<List<IN>> = aggregate(identity("Aggregate"))
fun <IN> OptionalSelector<IN>.aggregate(): SingleSelector<List<IN>> = aggregate(identity("Aggregate"))

fun <IN> ManySelector<IN>.count() = aggregate(CountFunction)

fun <IN> OptionalSelector<IN>.isPresent() = aggregate(ExistFunction)
fun <IN> ManySelector<IN>.exists() = aggregate(ExistFunction)

fun <IN> OptionalSelector<IN>.notPresent() = aggregate(NotExistFunction)
fun <IN> ManySelector<IN>.noneExists() = aggregate(NotExistFunction)
