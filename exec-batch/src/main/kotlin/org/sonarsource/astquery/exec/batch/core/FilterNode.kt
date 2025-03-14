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

package org.sonarsource.astquery.exec.batch.core

import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.batch.BatchNode
import org.sonarsource.astquery.exec.batch.ChildNode
import org.sonarsource.astquery.exec.batch.Signal
import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo

class FilterNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
  private val predicate: (IN) -> Boolean,
) : BatchNode<IN, IN>(id, children) {

  override val isSink = false

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Signal.Value<IN>) {
    propagate(context, value.apply { values -> values.filter(predicate) })
  }

  override fun getFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>) =
    if (getParentFlowType(parentsInfo) == FlowType.Many) FlowType.Many else FlowType.Opt

  override fun toString() = "Filter-$id"
}
