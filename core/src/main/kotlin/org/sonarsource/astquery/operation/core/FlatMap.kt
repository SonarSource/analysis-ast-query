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
import org.sonarsource.astquery.ir.nodes.FlatMap
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.operation.Operation1toN
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.idFunction

private typealias IdFunc<T> = IdentifiedFunction<T>
private typealias IdLambda<T> = IdentifiedLambda<T>

class FlatMapOperation<FROM, TO>(
  private val mapper: IdentifiedFunction<(FROM) -> Sequence<TO>>
) : Operation1toN<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    return FlatMap(parent, mapper)
  }
}

fun <FROM, TO> PipelineBuilder<FROM, *>.flatMapSeq(mapper: IdFunc<(FROM) -> Sequence<TO>>): ManyBuilder<TO> {
  return apply(FlatMapOperation(mapper))
}

fun <FROM, TO> PipelineBuilder<FROM, *>.flatMapSeq(mapper: (FROM) -> Sequence<TO>) =
  flatMapSeq(idFunction(lambda = mapper))

fun <FROM, TO> PipelineBuilder<FROM, *>.flatMap(mapper: IdLambda<(FROM) -> Collection<TO>>) =
  flatMapSeq(IdLambda(mapper.id, mapper.desc) { mapper.function(it).asSequence() })

fun <FROM, TO> PipelineBuilder<FROM, *>.flatMap(mapper: (FROM) -> Collection<TO>) =
  flatMap(idFunction(lambda = mapper))
