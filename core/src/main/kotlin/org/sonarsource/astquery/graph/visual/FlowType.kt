package org.sonarsource.astquery.graph.visual

enum class FlowType {
  Single, Opt, Many, ERR;

  infix operator fun plus(other: FlowType): FlowType {
    return when {
      this == ERR || other == ERR -> ERR
      this == Single && other == Single -> Single
      this == Many || other == Many -> Many
      else -> Opt
    }
  }

  infix operator fun minus(other: FlowType): FlowType {
    return when {
      this == ERR || other == ERR -> ERR
      this == Many && other == Many -> Many
      this == Single || other == Single -> Single
      else -> Opt
    }
  }
}
