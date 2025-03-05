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

package org.sonar.plugins.java.api.query.graph.ir.nodes

import org.sonar.plugins.java.api.query.graph.ir.IdentifiedFunction
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

class IRMap<IN, OUT>(
  parent: ParentNode<IN>,
  val mapper: IdentifiedFunction<(IN) -> OUT>
) : IRNode<IN, OUT>(parent) {

  override val isSink = false

  override fun canMergeWith(other: IRNode<*, *>): Boolean =
    other is IRMap<*, *> && mapper == other.mapper

  override fun copy() = IRMap(parents.single(), mapper)

  override fun getFlowType(parentsInfo: Map<IRNode<*, *>, VisualInfo>) =
    getParentFlowType(parentsInfo)

  override fun toString(): String = "Map(${mapper.name})"
}
