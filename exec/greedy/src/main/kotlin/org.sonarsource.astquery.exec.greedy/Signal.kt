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

package org.sonar.plugins.java.api.query.graph.exec.greedy

import org.sonar.plugins.java.api.query.graph.ScopeId

sealed interface Signal<out T> {
  data class Value<out T>(val value: T) : Signal<T>
  data class BatchEnd(val creator: ScopeId, val disabledBy: Set<ScopeId>) : Signal<Nothing> {

    fun disabledBy(scope: ScopeId) = copy(disabledBy = disabledBy + scope)

    fun enabledBy(scopes: Set<ScopeId>) = copy(disabledBy = disabledBy - scopes)

    fun mergeWith(other: BatchEnd): BatchEnd {
      require(creator == other.creator) { "Cannot merge batch starts from different creators" }

      return copy(disabledBy = disabledBy.intersect(other.disabledBy))
    }

    /** True if this batch start is not part of the current scope */
    val isActive = disabledBy.isEmpty()
  }
}
