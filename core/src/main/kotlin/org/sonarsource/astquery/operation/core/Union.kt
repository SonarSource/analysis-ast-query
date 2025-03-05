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

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.Selector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.ParentNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.Union
import org.sonar.plugins.java.api.query.operation.Operation1toN

class UnionOperation<FROM>(
  private val other: ParentNode<FROM>
) : Operation1toN<FROM, FROM> {

  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, FROM> {
    return Union(setOf(parent, other))
  }
}

infix fun <CUR : OUT, OUT> SingleSelector<CUR>.union(other: Selector<out OUT, *>): ManySelector<OUT> {
  return apply(UnionOperation(other.current))
}

infix fun <CUR : OUT, OUT> OptionalSelector<CUR>.union(other: Selector<out OUT, *>): ManySelector<OUT> {
  return apply(UnionOperation(other.current))
}

infix fun <CUR : OUT, OUT> ManySelector<CUR>.union(other: Selector<out OUT, *>): ManySelector<OUT> {
  return apply(UnionOperation(other.current))
}

fun <CUR> union(vararg selectors: Selector<CUR, *>): ManySelector<CUR> {
  require(selectors.isNotEmpty()) { "At least one selector must be provided" }

  return ManySelector(Union(selectors.map { it.current }.toSet()))
}
