package org.sonar.plugins.java.api.query.graph.exec.greedy.core

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.ScopeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.Store
import org.sonar.plugins.java.api.query.graph.exec.greedy.GreedyNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal.BatchEnd
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal.Value
import org.sonar.plugins.java.api.query.graph.exec.greedy.core.UnionState.Base
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

class UnionNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
  private val leftId: NodeId,
  private val rightId: NodeId,
  private val commonScopes: Set<ScopeId>,
) : GreedyNode<IN, IN>(id, children) {

  override val isSink = false

  private val currentState = Store<UnionState<IN>> { Base(commonScopes) }

  override fun onValue(context: ExecutionContext, caller: NodeId, value: IN) {
    val state = currentState.get(context)
    val side = getSide(caller)
    val shouldPropagate = state.onValue(value, side)

    if (shouldPropagate) {
      propagateValue(context, value)
    }
  }

  private fun getSide(caller: NodeId): Side {
    return when (caller) {
      leftId -> Side.LEFT
      rightId -> Side.RIGHT
      else -> error("Unexpected caller: $caller")
    }
  }

  override fun onBatchEnd(context: ExecutionContext, caller: NodeId, signal: BatchEnd) {
    if (signal.isActive) {
      markAsIncomplete(context)
    }

    val state = currentState.get(context)
    val side = getSide(caller)

    val (newState, toPropagate) = state.onBatchStart(signal, side)
    currentState.set(context, newState)

    for (signal in toPropagate) {
      when (signal) {
        is BatchEnd -> propagateBatchEnd(context, signal)
        is Value -> propagateValue(context, signal.value)
      }
    }
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) = FlowType.Many

  override fun toString() = "Union-$id"
}
