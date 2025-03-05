package org.sonar.plugins.java.api.query.graph

import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

/**
 * Interface common to all graph nodes
 */
interface Node<N : Node<N>> {

  /**
   * Get the id of the node
   */
  val id: NodeId

  /**
   * Get the children of the node
   */
  val children: List<N>

  /**
   * Is true if the node is an exit point of the execution
   */
  val isSink: Boolean

  /**
   * Get the visualisation information of the node given all its parents' information
   */
  fun getVisualizationInfo(parentsInfo: Map<N, VisualInfo>): VisualInfo
}
