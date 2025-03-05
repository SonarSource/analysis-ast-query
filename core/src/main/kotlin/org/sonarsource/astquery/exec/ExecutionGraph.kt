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

package org.sonarsource.astquery.exec

import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.graph.Node

interface Executable<IN> {

  fun execute(context: ExecutionContext, input: IN)

  fun isEmpty(): Boolean
}

abstract class ExecutionGraph<IN, R: N, N: Node<N>>(root: R) : Executable<IN>, Graph<R, N>(root) {
  override fun isEmpty(): Boolean = sinks.isEmpty()
}
