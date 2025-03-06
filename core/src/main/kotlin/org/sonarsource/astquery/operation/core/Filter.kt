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
import org.sonarsource.astquery.ir.nodes.Filter
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.idFunction

class FilterOperation<T>(
  private val predicate: IdentifiedFunction<(T) -> Boolean>
) : Operation1toOptional<T, T> {
  override fun applyTo(parent: ParentNode<T>): IRNode<*, out T> {
    return Filter(parent, predicate)
  }
}

fun <CUR> SingleBuilder<CUR>.filter(predicate: IdentifiedFunction<(CUR) -> Boolean>): OptionalBuilder<CUR> {
  return apply(FilterOperation(predicate))
}

fun <CUR> OptionalBuilder<CUR>.filter(predicate: IdentifiedFunction<(CUR) -> Boolean>): OptionalBuilder<CUR> {
  return apply(FilterOperation(predicate))
}

fun <CUR> ManyBuilder<CUR>.filter(predicate: IdentifiedFunction<(CUR) -> Boolean>): ManyBuilder<CUR> {
  return apply(FilterOperation(predicate))
}

fun <CUR> SingleBuilder<CUR>.filter(predicate: (CUR) -> Boolean) =
  filter(idFunction(lambda = predicate))

fun <CUR> OptionalBuilder<CUR>.filter(predicate: (CUR) -> Boolean) =
  filter(idFunction(lambda = predicate))

fun <CUR> ManyBuilder<CUR>.filter(predicate: (CUR) -> Boolean) =
  filter(idFunction(lambda = predicate))
