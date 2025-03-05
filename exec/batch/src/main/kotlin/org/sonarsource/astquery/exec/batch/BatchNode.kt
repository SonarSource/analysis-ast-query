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

package org.sonarsource.astquery.exec.batch

import org.sonar.plugins.java.api.query.graph.Node
import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

typealias ChildNode<T> = BatchNode<in T, *>

/**
 * This abstract [Node] subclass implements children. Those are nodes that will be executed
 * with the result of this node's execution.
 */
abstract class BatchNode<IN, OUT>(
  override val id: NodeId,
  override val children: List<ChildNode<OUT>>
) : Node<BatchNode<*, *>> {

  private fun onSignal(context: ExecutionContext, caller: NodeId, signal: Signal<IN>) {
    when (signal) {
      is Signal.Value -> onValue(context, caller, signal)
      is Signal.ScopeEnd -> onScopeEnd(context, caller, signal)
    }
  }

  protected open fun onScopeEnd(context: ExecutionContext, caller: NodeId, signal: Signal.ScopeEnd) {
    propagate(context, signal)
  }

  abstract fun onValue(context: ExecutionContext, caller: NodeId, value: Signal.Value<IN>)

  protected fun propagate(context: ExecutionContext, signal: Signal<OUT>) {
    children.forEach {
      it.onSignal(context, id, signal)
    }
  }

  override fun getVisualizationInfo(parentsInfo: Map<BatchNode<*, *>, VisualInfo>): VisualInfo {
    return VisualInfo(
      id = id.toString(),
      name = toString(),
      flowType = getFlowType(parentsInfo)
    )
  }

  abstract fun getFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>): FlowType

  protected fun getParentFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>) =
    parentsInfo.filterKeys { this in it.children }.values.single().flowType
}
