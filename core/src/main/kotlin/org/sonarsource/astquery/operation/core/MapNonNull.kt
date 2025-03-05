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

package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.SingleSelector

fun <FROM, TO> SingleSelector<FROM>.mapNonNull(mapper: IdentifiedFunction<(FROM) -> TO?>) =
  map(mapper).filterNonNull()
fun <FROM, TO> OptionalSelector<FROM>.mapNonNull(mapper: IdentifiedFunction<(FROM) -> TO?>) =
  map(mapper).filterNonNull()
fun <FROM, TO> ManySelector<FROM>.mapNonNull(mapper: IdentifiedFunction<(FROM) -> TO?>) =
  map(mapper).filterNonNull()

fun <FROM, TO> SingleSelector<FROM>.mapNonNull(mapper: (FROM) -> TO?) = map(mapper).filterNonNull()
fun <FROM, TO> OptionalSelector<FROM>.mapNonNull(mapper: (FROM) -> TO?) = map(mapper).filterNonNull()
fun <FROM, TO> ManySelector<FROM>.mapNonNull(mapper: (FROM) -> TO?) = map(mapper).filterNonNull()
