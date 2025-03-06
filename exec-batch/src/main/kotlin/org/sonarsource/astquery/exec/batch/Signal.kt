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

package org.sonarsource.astquery.exec.batch

import org.sonarsource.astquery.graph.ScopeId

sealed interface Signal<out T> {
  data class Value<T>(
    val values: List<T>,
    val scopes: Scopes,
  ) : Signal<T> {

    fun unscopedFrom(scopeIds: Set<ScopeId>): Value<T> {
      return Value(values, scopes.unscopedFrom(scopeIds))
    }

    fun <R> apply(mapper: (List<T>) -> List<R>): Value<R> {
      return Value(mapper(values), scopes)
    }

    operator fun plus(other: Value<T>): Value<T> {
      return Value(values + other.values, scopes + other.scopes)
    }
  }

  data class ScopeEnd(val scope: ScopeId, val scopes: Scopes) : Signal<Nothing>
}
