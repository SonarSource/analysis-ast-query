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

package org.sonarsource.astquery.exec.greedy.specialized.first

import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.greedy.ChildNode
import org.sonarsource.astquery.exec.greedy.GreedyNode
import org.sonarsource.astquery.exec.greedy.Signal
import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo

class FirstOrDefaultNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
  val defaultValue: IN,
) : GreedyNode<IN, IN>(id, children) {

  override val isSink = false

  override fun onValue(context: ExecutionContext, caller: NodeId, value: IN) {
    propagateValue(context, value)
    markAsComplete(context)
  }

  override fun onBatchEnd(context: ExecutionContext, caller: NodeId, signal: Signal.BatchEnd) {
    if (signal.isActive && !isNodeComplete(context)) {
        propagateValue(context, defaultValue)
    }

    super.onBatchEnd(context, caller, signal)
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) = FlowType.Single

  override fun toString() = "FirstOrDefault($defaultValue)-$id"
}
