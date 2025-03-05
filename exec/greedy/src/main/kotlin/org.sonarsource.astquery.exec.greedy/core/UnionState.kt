package org.sonar.plugins.java.api.query.graph.exec.greedy.core

import org.sonar.plugins.java.api.query.graph.ScopeId
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal.BatchEnd
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal.Value

enum class Side(val value: String) {
  LEFT("left"), RIGHT("right")
}

sealed interface UnionState<T> {

  fun onValue(signal: T, side: Side): Boolean

  fun onBatchStart(newBatch: BatchEnd, side: Side): Pair<UnionState<T>, List<Signal<T>>>

  class Base<T>(
    val commonScopes: Set<ScopeId>
  ) : UnionState<T> {

    override fun onValue(signal: T, side: Side): Boolean {
      return true
    }

    override fun onBatchStart(newBatch: BatchEnd, side: Side): Pair<UnionState<T>, List<Signal<T>>> {
      if (newBatch.creator !in commonScopes) {
        return this to listOf(newBatch)
      }

      return SidePending<T>(
        commonScopes,
        side,
        mutableListOf(newBatch),
      ) to emptyList()
    }
  }

  class SidePending<T>(
    val commonScopes: Set<ScopeId>,
    val pendingSide: Side,
    val pending: MutableList<Signal<T>>,
  ) : UnionState<T> {

    override fun onValue(value: T, side: Side): Boolean {
      return if (pendingSide == side) {
        pending.add(Value(value))
        false
      } else {
        true
      }
    }

    override fun onBatchStart(newBatch: BatchEnd, side: Side): Pair<UnionState<T>, List<Signal<T>>> {
      if (pendingSide == side) {
        pending.add(newBatch)
        return this to emptyList()
      }

      if (newBatch.creator !in commonScopes) {
        return this to listOf(newBatch)
      }

      val pendingBatch = pending.removeFirst() as BatchEnd
      val mergedBatch = newBatch.mergeWith(pendingBatch)

      val toPropagate = pending.takeWhile {  signal ->
        signal is Value<T> || (signal is BatchEnd && signal.creator !in commonScopes)
      }

      val nextState = if (pending.isNotEmpty()) {
        // There are signals still pending
        SidePending<T>(
          commonScopes,
          side,
          pending,
        )
      } else {
        // All pending signals have been processed
        Base(commonScopes)
      }

      return nextState to listOf(mergedBatch) + toPropagate
    }
  }
}
