package org.sonar.plugins.java.api.query.graph.exec.greedy.core

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.greedy.GreedyNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

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
