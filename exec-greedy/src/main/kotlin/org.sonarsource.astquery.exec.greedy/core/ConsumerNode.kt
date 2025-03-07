package org.sonarsource.astquery.exec.greedy.core

import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.greedy.ChildNode
import org.sonarsource.astquery.exec.greedy.GreedyNode
import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.visual.VisualInfo

class ConsumerNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
  private val consumerFunc: (ExecutionContext, IN) -> Unit,
) : GreedyNode<IN, IN>(id, children) {

  override val isSink = true

  override fun onValue(context: ExecutionContext, caller: NodeId, value: IN) {
    consumerFunc(context, value)
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) = getParentFlowType(parentsInfo)

  override fun toString() = "Consumer-$id"
}
