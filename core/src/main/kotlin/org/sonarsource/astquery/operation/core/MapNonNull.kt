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
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder

fun <FROM, TO> SingleBuilder<FROM>.mapNonNull(mapper: IdentifiedFunction<(FROM) -> TO?>) =
  map(mapper).filterNonNull()
fun <FROM, TO> OptionalBuilder<FROM>.mapNonNull(mapper: IdentifiedFunction<(FROM) -> TO?>) =
  map(mapper).filterNonNull()
fun <FROM, TO> ManyBuilder<FROM>.mapNonNull(mapper: IdentifiedFunction<(FROM) -> TO?>) =
  map(mapper).filterNonNull()

fun <FROM, TO> SingleBuilder<FROM>.mapNonNull(mapper: (FROM) -> TO?) = map(mapper).filterNonNull()
fun <FROM, TO> OptionalBuilder<FROM>.mapNonNull(mapper: (FROM) -> TO?) = map(mapper).filterNonNull()
fun <FROM, TO> ManyBuilder<FROM>.mapNonNull(mapper: (FROM) -> TO?) = map(mapper).filterNonNull()
