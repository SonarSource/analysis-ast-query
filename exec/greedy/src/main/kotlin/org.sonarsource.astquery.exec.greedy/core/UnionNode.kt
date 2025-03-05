package org.sonarsource.astquery.exec.greedy.core

import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.ScopeId
import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.Store
import org.sonarsource.astquery.exec.greedy.GreedyNode
import org.sonarsource.astquery.exec.greedy.ChildNode
import org.sonarsource.astquery.exec.greedy.Signal.BatchEnd
import org.sonarsource.astquery.exec.greedy.Signal.Value
import org.sonarsource.astquery.exec.greedy.core.UnionState.Base
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo

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
