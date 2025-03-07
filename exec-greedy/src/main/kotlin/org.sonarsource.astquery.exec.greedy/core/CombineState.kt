package org.sonarsource.astquery.exec.greedy.core

import org.sonarsource.astquery.exec.greedy.Signal
import org.sonarsource.astquery.exec.greedy.Signal.BatchEnd
import org.sonarsource.astquery.exec.greedy.Signal.Value
import org.sonarsource.astquery.graph.ScopeId

sealed interface CombineState<LT, RT> {

  val leftBuffer: List<Signal<LT>>
  val rightBuffer: List<Signal<RT>>

  fun onValueLeft(signal: Value<LT>): Boolean
  fun onValueRight(signal: Value<RT>): Boolean

  fun onBatchStartLeft(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>>
  fun onBatchStartRight(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>>

  class Base<LT, RT>(
    val commonScopes: Set<ScopeId>,
    override val leftBuffer: MutableList<Signal<LT>> = mutableListOf(),
    override val rightBuffer: MutableList<Signal<RT>> = mutableListOf(),
  ) : CombineState<LT, RT> {

    override fun onValueLeft(signal: Value<LT>): Boolean {
      leftBuffer.add(signal)
      return true
    }

    override fun onValueRight(signal: Value<RT>): Boolean {
      rightBuffer.add(signal)
      return true
    }

    override fun onBatchStartLeft(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>> {
      if (newBatch.creator !in commonScopes) {
        leftBuffer.add(newBatch)
        // Only propagate if there are buffered right signals
        return this to (if (rightBuffer.isNotEmpty()) listOf(newBatch) else emptyList())
      }

      val toPropagate = if (leftBuffer.isEmpty()) {
        // If the left buffer is empty, then all batch start signal from right have been buffered without
        // Being pushed. We need to push them now.
        rightBuffer.filterIsInstance<BatchEnd>()
      } else {
        emptyList()
      }

      return LeftPending(
        commonScopes,
        leftBuffer,
        rightBuffer,
        mutableListOf(newBatch),
      ) to toPropagate
    }

    override fun onBatchStartRight(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>> {
      if (newBatch.creator !in commonScopes) {
        rightBuffer.add(newBatch)
        // Only propagate if there are buffered right signals
        return this to (if (leftBuffer.isNotEmpty()) listOf(newBatch) else emptyList())
      }

      val toPropagate = if (rightBuffer.isEmpty()) {
        // If the left buffer is empty, then all batch start signal from right have been buffered without
        // Being pushed. We need to push them now.
        leftBuffer.filterIsInstance<BatchEnd>()
      } else {
        emptyList()
      }

      return RightPending(
        commonScopes,
        leftBuffer,
        rightBuffer,
        mutableListOf(newBatch),
      ) to toPropagate
    }
  }

  class LeftPending<LT, RT>(
    val commonScopes: Set<ScopeId>,
    override val leftBuffer: List<Signal<LT>>,
    override val  rightBuffer: MutableList<Signal<RT>>,
    val pending: MutableList<Signal<LT>>,
  ) : CombineState<LT, RT> {

    override fun onValueLeft(signal: Value<LT>): Boolean {
      pending.add(signal)
      return false
    }

    override fun onValueRight(signal: Value<RT>): Boolean {
      rightBuffer.add(signal)
      return true
    }

    override fun onBatchStartLeft(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>> {
      pending.add(newBatch)
      return this to emptyList()
    }

    override fun onBatchStartRight(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>> {
      if (newBatch.creator !in commonScopes) {
        return this to listOf(newBatch)
      }

      val leftBatch = pending.removeFirst() as BatchEnd
      val mergedBatch = newBatch.mergeWith(leftBatch)

      val newLeftBuffer = pending.takeWhile { signal ->
        signal is Value<LT> || (signal is BatchEnd && signal.creator !in commonScopes)
      }

      val nextState = if (pending.size > newLeftBuffer.size) {
        // There are left signals still pending
        LeftPending(
          commonScopes,
          newLeftBuffer.toMutableList(),
          mutableListOf<Signal<RT>>(),
          pending.drop(newLeftBuffer.size).toMutableList(),
        )
      } else {
        // All left signals have been processed
        Base(
          commonScopes,
          mutableListOf<Signal<LT>>(),
          mutableListOf<Signal<RT>>(),
        )
      }

      return nextState to listOf(mergedBatch)
    }
  }

  class RightPending<LT, RT>(
    val commonScopes: Set<ScopeId>,
    override val leftBuffer: MutableList<Signal<LT>>,
    override val  rightBuffer: List<Signal<RT>>,
    val pending: MutableList<Signal<RT>>,
  ) : CombineState<LT, RT> {

    override fun onValueLeft(signal: Value<LT>): Boolean {
      leftBuffer.add(signal)
      return true
    }

    override fun onValueRight(signal: Value<RT>): Boolean {
      pending.add(signal)
      return false
    }

    override fun onBatchStartLeft(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>> {
      if (newBatch.creator !in commonScopes) {
        // Only propagate if there are buffered right signals
        return this to listOf(newBatch)
      }

      val rightBatch = pending.removeFirst() as BatchEnd
      val mergedBatch = newBatch.mergeWith(rightBatch)

      val newRightBuffer = pending.takeWhile { signal ->
        signal is Value<RT> || (signal is BatchEnd && signal.creator !in commonScopes)
      }

      val nextState = if (pending.size > newRightBuffer.size) {
        // There are right signals still pending
        RightPending(
          commonScopes,
          mutableListOf<Signal<LT>>(),
          newRightBuffer.toMutableList(),
          pending.drop(newRightBuffer.size).toMutableList(),
        )
      } else {
        // All right signals have been processed
        Base(
          commonScopes,
          mutableListOf<Signal<LT>>(),
          mutableListOf<Signal<RT>>(),
        )
      }

      return nextState to listOf(mergedBatch)
    }

    override fun onBatchStartRight(newBatch: BatchEnd): Pair<CombineState<LT, RT>, List<BatchEnd>> {
      pending.add(newBatch)
      return this to emptyList()
    }
  }
}
