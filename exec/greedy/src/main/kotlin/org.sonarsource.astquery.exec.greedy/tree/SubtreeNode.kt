/*
 * SonarQube Java
 * Copyright (C) 2012-2024 SonarSource SA
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

package org.sonar.plugins.java.api.query.graph.exec.greedy.tree

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.greedy.GreedyNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.greedy.Signal
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo
import org.sonar.plugins.java.api.tree.BaseTreeVisitor
import org.sonar.plugins.java.api.tree.Tree
import org.sonar.plugins.java.api.tree.Tree.Kind

class SubtreeNode(
  id: NodeId,
  children: List<ChildNode<Tree>>,
  val stopAt: Set<Kind>,
  val includeStart: Boolean,
) : GreedyNode<Tree, Tree>(id, children) {

  override val isSink = false

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Tree) {
    val visitor = Visitor(stopAt)

    if (includeStart) {
      propagateValue(context, value)
    }

    value.accept(visitor)

    for (tree in visitor.collector) {
      propagateValue(context, tree)
    }
  }

  private class Visitor(
    private val stopAt: Set<Kind>
  ) : BaseTreeVisitor() {

    val collector = mutableListOf<Tree>()

    override fun scan(tree: Tree?) {
      tree ?: return

      collector.add(tree)

      if (!tree.`is`(*stopAt.toTypedArray())) {
        super.scan(tree)
      }
    }
  }

  override fun getFlowType(parentsInfo: Map<GreedyNode<*, *>, VisualInfo>) = FlowType.Many

  override fun toString() = "${if (includeStart) "Tree" else "Subtree"}${stopAt.joinToString(", ", "(", ")")}-$id"
}
