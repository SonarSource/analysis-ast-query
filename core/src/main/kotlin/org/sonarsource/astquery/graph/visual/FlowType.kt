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
