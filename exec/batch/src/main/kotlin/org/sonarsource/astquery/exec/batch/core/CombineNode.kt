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

import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.Store
import org.sonarsource.astquery.exec.batch.BatchNode
import org.sonarsource.astquery.exec.batch.ChildNode
import org.sonarsource.astquery.exec.batch.MergeSignalHelper
import org.sonarsource.astquery.exec.batch.Signal
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo

class CombineNode<LT, RT, OUT>(
  id: NodeId,
  children: List<ChildNode<OUT>>,
  private val leftId: NodeId,
  private val rightId: NodeId,
  private val combiner: (LT, RT) -> OUT,
) : BatchNode<Any?, OUT>(id, children) {

  override val isSink = false

  private val leftQueue = Store { mutableListOf<Signal<LT>>() }
  private val rightQueue = Store { mutableListOf<Signal<RT>>() }

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Signal.Value<Any?>) {
    addToQueue(context, caller, value)
  }

  override fun onScopeEnd(context: ExecutionContext, caller: NodeId, signal: Signal.ScopeEnd) {
    addToQueue(context, caller, signal)

    val toProcess = mutableListOf<Signal<OUT>>()

    val newQueues = MergeSignalHelper.processScope(
      leftQueue.get(context),
      rightQueue.get(context),
      signal,
      { left, right, _ -> toProcess.add(combineValues(left, right)) },
      { signal -> toProcess.add(signal) }
    )

    if (newQueues == null) {
      return
    }

    toProcess.forEach { propagate(context, it) }

    val (newQueueLeft, newQueueRight) = newQueues
    leftQueue.set(context, newQueueLeft.toMutableList())
    rightQueue.set(context, newQueueRight.toMutableList())
  }

  private fun combineValues(left: Signal.Value<LT>, rights: Signal.Value<RT>): Signal.Value<OUT> {
    val scopes = left.scopes + rights.scopes
    val combined = left.values.flatMap { leftValue ->
      rights.values.map { rightValue -> combiner(leftValue, rightValue) }
    }

    return Signal.Value(combined, scopes)
  }

  private fun addToQueue(context: ExecutionContext, caller: NodeId, signal: Signal<*>) {
    @Suppress("UNCHECKED_CAST")
    when (caller) {
      leftId -> leftQueue.get(context).add(signal as Signal<LT>)
      rightId -> rightQueue.get(context).add(signal as Signal<RT>)
      else -> error("Unexpected caller: $caller")
    }
  }

  override fun getFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>) = FlowType.Many

  override fun toString() = "Combine-$id"
}
