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
package org.sonarsource.astquery.exec.transformation

import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.graph.GraphUtils.breathFirst
import org.sonarsource.astquery.ir.IR

class RemoveUnusedNodes: Transformation<IR> {

    override fun <R : IR> apply(input: Graph<R, IR>): Graph<R, IR> {
        breathFirst(input.root)
            .filter { !it.hasSink }
            .toList()
            .forEach { it.delete() }

        return Graph(input.root)
    }
}