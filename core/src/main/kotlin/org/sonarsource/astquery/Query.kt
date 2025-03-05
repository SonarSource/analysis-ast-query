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

package org.sonar.plugins.java.api.query

import org.sonar.plugins.java.api.JavaFileScannerContext
import org.sonar.plugins.java.api.query.graph.exec.greedy.GreedyBuilder
import org.sonar.plugins.java.api.query.graph.ir.nodes.Root

fun interface Query<in INPUT, out OUTPUT> {

  fun execute(context: ExecutionContext, input: INPUT): OUTPUT

  companion object {
    fun <INPUT, OUTPUT> of(builder: (SingleSelector<INPUT>) -> Selector<*, OUTPUT>): Query<INPUT, OUTPUT> {
      val root = Root<INPUT>()
      val selector = SingleSelector(root)
      val output = builder(selector)

      return GreedyBuilder().buildQuery(root, output)
    }
  }
}
