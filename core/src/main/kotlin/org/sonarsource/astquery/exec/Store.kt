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

import java.util.concurrent.ConcurrentHashMap

class Store<T>(
  private val defaultValue: () -> T
) {

  private val values = ConcurrentHashMap<ExecutionContext, T>()

  fun has(context: ExecutionContext): Boolean {
    return values.containsKey(context)
  }

  fun get(context: ExecutionContext): T {
    return values.getOrPut(context, defaultValue)
  }

  fun set(context: ExecutionContext, value: T) {
    values[context] = value
  }

  fun remove(context: ExecutionContext): T {
    return if (values.containsKey(context)) {
      values.remove(context)!!
    } else {
      defaultValue()
    }
  }
}
