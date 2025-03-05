package org.sonar.plugins.java.api.query.graph.visual

enum class FlowType {
  Single, Opt, Many, ERR;

  infix operator fun plus(other: FlowType): FlowType {
    return when {
      this == Single && other == Single -> Single
      this == Many || other == Many -> Many
      else -> Opt
    }
  }

  infix operator fun minus(other: FlowType): FlowType {
    return when {
      this == Many && other == Many -> Many
      this == Single || other == Single -> Single
      else -> Opt
    }
  }
}
