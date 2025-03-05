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

package org.sonar.plugins.java.api.query.graph.exec.greedy.specialized

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.Store
import org.sonar.plugins.java.api.query.graph.exec.greedy.GreedyNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

class CountNode<IN>(
  id: NodeId,
  children: List<ChildNode<NodeId>>,
) : GreedyNode<IN, Int>(id, children) {

  override val isSink = false

  /** Stores the values of the current batch until a new batch start to aggregate them. */
  private val count = Store { 0 }

  override fun onValue(context: ExecutionContext, caller: NodeId, value: IN) {
    count.set(context, count.get(context) + 1)
  }

  override fun onBatchEnd(context: ExecutionContext, caller: NodeId, signal: Signal.BatchEnd) {
    if (signal.isActive && !isNodeComplete(context)) {
      propagateValue(context, count.remove(context))
    }

    super.onBatchEnd(context, caller, signal)
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) = FlowType.Single

  override fun toString() = "Count-$id"
}
