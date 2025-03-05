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

package org.sonar.plugins.java.api.query.graph.exec.greedy.core

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.greedy.GreedyNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo
import kotlin.reflect.KClass

class FilterTypeNode<IN, OUT : IN & Any>(
  id: NodeId,
  children: List<ChildNode<OUT>>,
  private val types: Set<KClass<out OUT>>,
) : GreedyNode<IN, OUT>(id, children) {

  override val isSink = false

  override fun onValue(context: ExecutionContext, caller: NodeId, value: IN) {
    if (types.any { it.isInstance(value) }) {
      @Suppress("UNCHECKED_CAST")
      propagateValue(context, value as OUT)
    }
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) =
    if (getParentFlowType(parentsInfo) == FlowType.Many) FlowType.Many else FlowType.Opt

  override fun toString() = "FilterType(${types.joinToString { it.simpleName ?: it.toString() }})-$id"
}
