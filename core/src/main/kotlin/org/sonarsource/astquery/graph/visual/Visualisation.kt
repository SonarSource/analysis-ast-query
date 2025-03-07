/*
 * Copyright (C) 2018-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.astquery.graph.visual

import org.sonarsource.astquery.graph.GraphUtils
import org.sonarsource.astquery.graph.Node
import kotlin.random.Random

object Visualisation {

  private fun <N : Node<N>> buildInfoMap(start: N): Map<N, VisualInfo> {
    val infoMap = mutableMapOf<N, VisualInfo>()

    GraphUtils.topologicalSort(start)
      .forEach { node ->
        val info = node.getVisualizationInfo(infoMap)
        infoMap[node] = info
      }

    return infoMap
  }

  fun <N : Node<N>> toMermaid(root: N, subGraph: Boolean = false): String {
    fun getArrow(flowType: FlowType): String {
      return when (flowType) {
        FlowType.Single -> "--->"
        FlowType.Opt -> "-..->"
        FlowType.Many -> "===>"
        FlowType.ERR -> "===> |ERR|"
      }
    }

    val infoMap = buildInfoMap(root)
    val links = infoMap.entries.joinToString("") { (node, info) ->
      node.children.map(infoMap::getValue).joinToString("") { child ->
        "\n${info.id} ${getArrow(info.flowType)} ${child.id}"
      } + info.secondaryLinks.joinToString("") { "\n${info.id} -.- $it" }
    }

    val chart = "flowchart TD\n" +
      infoMap.values.joinToString("\n") { nodeDef(it) } +
      "\n" +
      links

    return if (subGraph) {
      "subgraph $chart \nend\n"
    } else {
      chart
    }
  }

  private fun nodeDef(info: VisualInfo): String =
    "${info.id}([\"${info.name}-${info.id}\"]); style ${info.id} fill:#${getColor(info)}"

  private fun getColor(info: VisualInfo): String = info.color ?: Random(info.id.hashCode()).run {
    val r = nextBits(7) or 0x80
    val g = nextBits(7) or 0x80
    val b = nextBits(7) or 0x80

    Integer.toHexString((((r shl 8) or g) shl 8) or b)
  }
}
