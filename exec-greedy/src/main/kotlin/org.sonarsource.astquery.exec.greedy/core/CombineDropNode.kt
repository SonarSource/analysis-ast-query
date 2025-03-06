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

package org.sonarsource.astquery.exec.greedy.core

import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.ScopeId
import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.Store
import org.sonarsource.astquery.exec.greedy.GreedyNode
import org.sonarsource.astquery.exec.greedy.ChildNode
import org.sonarsource.astquery.exec.greedy.Signal.BatchEnd
import org.sonarsource.astquery.exec.greedy.Signal.Value
import org.sonarsource.astquery.exec.greedy.core.CombineState.Base
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo
import org.sonarsource.astquery.operation.Droppable

class CombineDropNode<LT, RT, OUT>(
  id: NodeId,
  children: List<ChildNode<OUT>>,
  private val leftId: NodeId,
  private val rightId: NodeId,
  private val commonScopes: Set<ScopeId>,
  private val combiner: (LT, RT) -> Droppable<OUT>,
) : GreedyNode<Any?, OUT>(id, children) {

  override val isSink = false

  private val currentState = Store<CombineState<LT, RT>> { Base(commonScopes) }

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Any?) {
    val state = currentState.get(context)
    @Suppress("UNCHECKED_CAST")
    when (caller) {
      leftId -> onLeftValue(context, value as LT, state)
      rightId -> onRightValue(context, value as RT, state)
      else -> error("Unexpected caller: $caller")
    }
  }

  private fun onLeftValue(context: ExecutionContext, value: LT, state: CombineState<LT, RT>) {
    for (right in state.rightBuffer) {
      when (right) {
        is Value<RT> -> {
          val result = combiner(value, right.value)
          if (result is Droppable.Keep) {
            propagateValue(context, result.data)
          }
        }
        is BatchEnd -> propagateBatchEnd(context, right)
      }
    }

    state.onValueLeft(Value(value))
  }

  private fun onRightValue(context: ExecutionContext, value: RT, state: CombineState<LT, RT>) {
    for (left in state.leftBuffer) {
      when (left) {
        is Value<LT> -> {
          val result = combiner(left.value, value)
          if (result is Droppable.Keep) {
            propagateValue(context, result.data)
          }
        }
        is BatchEnd -> propagateBatchEnd(context, left)
      }
    }

    state.onValueRight(Value(value))
  }

  override fun onBatchEnd(context: ExecutionContext, caller: NodeId, signal: BatchEnd) {
    markAsIncomplete(context)

    val curState = currentState.get(context)
    val (newState, toPropagate) = when (caller) {
      leftId -> curState.onBatchStartLeft(signal)
      rightId -> curState.onBatchStartRight(signal)
      else -> error("Unexpected caller: $caller")
    }

    currentState.set(context, newState)

    toPropagate.forEach { propagateBatchEnd(context, it) }
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) = FlowType.Many

  override fun toString() = "CombineDrop-$id"
}
