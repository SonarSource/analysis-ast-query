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

package org.sonar.plugins.java.api.query.graph.exec.greedy

import org.sonar.plugins.java.api.query.graph.Node
import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.Store
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal.BatchEnd
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

typealias ChildNode<T> = GreedyNode<in T, *>

/**
 * This abstract [Node] subclass implements children. Those are nodes that will be executed
 * with the result of this node's execution.
 */
abstract class GreedyNode<IN, OUT>(
  override val id: NodeId,
  override val children: List<ChildNode<OUT>>
) : Node<GreedyNode<*, *>> {

  private val isComplete = Store { false }

  abstract fun onValue(context: ExecutionContext, caller: NodeId, value: IN)

  protected open fun onBatchEnd(context: ExecutionContext, caller: NodeId, signal: BatchEnd) {
    if (signal.isActive) {
      markAsIncomplete(context)
    }

    propagateBatchEnd(context, signal)
  }

  protected fun propagateValue(context: ExecutionContext, value: OUT) {
    var allComplete = true

    children
      .filter { !it.isNodeComplete(context) }
      .forEach { child ->
        child.onValue(context, id, value)
        allComplete = allComplete && child.isNodeComplete(context)
      }

    if (allComplete) {
      markAsComplete(context)
    }
  }

  protected fun propagateBatchEnd(context: ExecutionContext, signal: BatchEnd) =
    children.forEach {
      it.onBatchEnd(context, id, signal)
    }

  protected fun isNodeComplete(context: ExecutionContext): Boolean {
    return isComplete.get(context)
  }

  protected open fun markAsComplete(context: ExecutionContext) {
    isComplete.set(context, true)
  }

  protected fun markAsIncomplete(context: ExecutionContext) {
    isComplete.remove(context)
  }

  override fun getVisualizationInfo(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>): VisualInfo {
    return VisualInfo(
      id = id.toString(),
      name = toString(),
      flowType = getFlowType(parentsInfo)
    )
  }

  abstract fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>): FlowType

  protected fun getParentFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) =
    parentsInfo.filterKeys { this in it.children }.values.single().flowType
}
