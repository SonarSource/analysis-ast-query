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

package org.sonarsource.astquery.exec.batch.core

import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonar.plugins.java.api.query.graph.ScopeId
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.exec.batch.BatchNode
import org.sonar.plugins.java.api.query.graph.exec.batch.ChildNode
import org.sonar.plugins.java.api.query.graph.exec.batch.Scopes
import org.sonar.plugins.java.api.query.graph.exec.batch.Signal
import org.sonar.plugins.java.api.query.graph.visual.FlowType
import org.sonar.plugins.java.api.query.graph.visual.VisualInfo

private const val ROOT_SCOPE_ID: ScopeId = -1

class RootNode<IN>(
  id: NodeId,
  children: List<ChildNode<IN>>,
) : BatchNode<IN, IN>(id, children) {

  override val isSink = false

  override fun onValue(context: ExecutionContext, caller: NodeId, value: Signal.Value<IN>) {
    propagate(context, value)
    propagate(context, Signal.ScopeEnd(
      ROOT_SCOPE_ID,
      Scopes(ROOT_SCOPE_ID to value.values.single())
    ))
  }

  override fun getFlowType(parentsInfo: Map<BatchNode<*, *>, VisualInfo>) = FlowType.Single

  override fun toString() = "Root-$id"
}
