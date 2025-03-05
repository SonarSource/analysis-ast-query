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

import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.Selector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.ir.nodes.Union
import org.sonarsource.astquery.operation.Operation1toN

class UnionOperation<FROM>(
  private val other: ParentNode<FROM>
) : Operation1toN<FROM, FROM> {

  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, FROM> {
    return Union(setOf(parent, other))
  }
}

infix fun <CUR : OUT, OUT> SingleSelector<CUR>.union(other: Selector<out OUT, *>): ManySelector<OUT> {
  return apply(UnionOperation(other.irNode))
}

infix fun <CUR : OUT, OUT> OptionalSelector<CUR>.union(other: Selector<out OUT, *>): ManySelector<OUT> {
  return apply(UnionOperation(other.irNode))
}

infix fun <CUR : OUT, OUT> ManySelector<CUR>.union(other: Selector<out OUT, *>): ManySelector<OUT> {
  return apply(UnionOperation(other.irNode))
}

fun <CUR> union(vararg selectors: Selector<CUR, *>): ManySelector<CUR> {
  require(selectors.isNotEmpty()) { "At least one selector must be provided" }

  return ManySelector(Union(selectors.map { it.irNode }.toSet()))
}
