package org.sonarsource.astquery.exec.batch.core

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.batch.BatchNode
import org.sonar.plugins.java.api.query.graph.exec.batch.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.batch.Signal
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

class ConsumerNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
  private val consumerFunc: (ExecutionContext, IN) -> Unit,
) : BatchNode<IN, IN>(id, children) {

  override val isSink = true

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Signal.Value<IN>) {
    value.values.forEach { v -> consumerFunc(context, v) }

    propagate(context, value)
  }

  override fun getFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>) = getParentFlowType(parentsInfo)

  override fun toString() = "Consumer-$id"
}
