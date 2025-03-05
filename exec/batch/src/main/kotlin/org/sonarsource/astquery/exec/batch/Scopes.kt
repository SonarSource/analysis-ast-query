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

import org.sonar.plugins.java.api.query.graph.ScopeId
import org.sonar.plugins.java.api.query.graph.exec.batch.core.ScopeNode

data class Scopes(
  private val scopes: Map<ScopeId, Any?> = emptyMap()
) {

  constructor(value: Pair<ScopeId, Any?>) : this(mapOf(value))

  fun <T> scopedTo(scope: ScopeNode<T>, value: T): Scopes =
    Scopes(scopes + (scope.scopeId to value))

  fun unscopedFrom(scopeIds: Set<ScopeId>): Scopes =
    Scopes(scopes - scopeIds)

  fun isSameAs(other: Scopes): Boolean {
    val commonKeys = scopes.keys.intersect(other.scopes.keys)
    return commonKeys.all { scopes[it] == other.scopes[it] }
  }

  operator fun plus(other: Scopes): Scopes {
    require(isSameAs(other)) { "Scopes $scopes and ${other.scopes} overlap with non-matching values" }
    return Scopes(scopes + other.scopes)
  }
}
