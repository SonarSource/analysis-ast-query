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
import org.sonarsource.astquery.graph.ScopeId
import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.Store
import org.sonarsource.astquery.exec.batch.BatchNode
import org.sonarsource.astquery.exec.batch.ChildNode
import org.sonarsource.astquery.exec.batch.Scopes
import org.sonarsource.astquery.exec.batch.Signal
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo

class UnScopeNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
  private val scopeIds: Set<ScopeId>
) : BatchNode<IN, IN>(id, children) {

  override val isSink = false

  private val scopeEnded = Store { mutableSetOf<ScopeId>() }
  private val queue = Store { mutableListOf<Signal<IN>>() }

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Signal.Value<IN>) {
    scopeEnded.remove(context)
    queue.get(context).add(value.unscopedFrom(scopeIds))
  }

  override fun onScopeEnd(context: ExecutionContext, caller: NodeId, signal: Signal.ScopeEnd) {
    val endedScopes = scopeEnded.get(context)

    if (signal.scope in scopeIds) {
      endedScopes.add(signal.scope)
    } else {
      addSignalToQueue(context, signal)
    }

    if (endedScopes.size < scopeIds.size) {
      return
    }

    val signalQueue = queue.remove(context)
    scopeEnded.remove(context)

    if (signalQueue.isEmpty()) {
      propagate(context, Signal.Value(emptyList(), signal.scopes))
      return
    }


    // Unscope all the values by merging similar scoped values together
    var lastScope: Scopes? = null
    var values: List<IN> = emptyList()

    val results = mutableListOf<Signal<IN>>()

    for (item in signalQueue) {
      when (item) {
        is Signal.ScopeEnd -> {
          // End of current scope if there are elements
          if (lastScope != null) {
            results += Signal.Value(values, lastScope)
            values = emptyList()
            lastScope = null
          }

          if (results.lastOrNull() != item) results += item
        }

        is Signal.Value -> {
          if (lastScope == null) {
            // First of current scope
            lastScope = item.scopes
          } else if (lastScope != item.scopes) {
            // End of current scope
            results += Signal.Value(values, lastScope)
            values = emptyList()
            lastScope = item.scopes
          }

          values = values + item.values
        }
      }
    }

    if (lastScope != null) {
      results += Signal.Value(values, lastScope)
    }

    results.forEach { propagate(context, it) }
  }

  private fun addSignalToQueue(context: ExecutionContext, signal: Signal<Nothing>) {
    val current = queue.get(context)
    if (current.isEmpty()) {
      // The unscope node only needs to unscope values, so if none has been stored, there is no need to block the signal
      // In fact, blocking it might generate issues
      propagate(context, signal)
    } else {
      queue.get(context).add(signal)
    }
  }

  override fun getFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>): FlowType {
    val scopesParent = parentsInfo.entries.filter { entry ->
      // Find the only node that has the scope in its children
      entry.key.children.any { it is ScopeNode<*> && it.scopeId in scopeIds }
    }

    val flows = scopesParent.map { it.value.flowType }
    return flows.reduce { acc, flow -> acc - flow }
  }

  override fun toString() = "UnScope($scopeIds)-$id"
}
