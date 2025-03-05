package org.sonar.plugins.java.api.query.graph.visual

data class VisualInfo(
  val id: String,
  val name: String,
  val flowType: FlowType,
  val secondaryLinks: Set<String> = emptySet(),
  val color: String? = null
)
