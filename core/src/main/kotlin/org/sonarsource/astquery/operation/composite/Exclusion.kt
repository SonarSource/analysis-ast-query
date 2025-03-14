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
package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.Droppable
import org.sonarsource.astquery.operation.Droppable.Drop
import org.sonarsource.astquery.operation.Droppable.Keep
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.core.combineFilter

private fun <T> exclusionFunction(): IdentifiedFunction<(T, List<T>) -> Droppable<T>> =
  IdentifiedLambda("exclusion", "Exclusion") { elem: T, excluded: List<T> ->
    if (excluded.contains(elem)) Drop else Keep(elem)
  }

fun <CUR> SingleBuilder<CUR>.exclude(other: SingleBuilder<List<CUR>>) =
  combineFilter(other, exclusionFunction())

fun <CUR> OptionalBuilder<CUR>.exclude(other: SingleBuilder<List<CUR>>) =
  combineFilter(other, exclusionFunction())

fun <CUR> ManyBuilder<CUR>.exclude(other: SingleBuilder<List<CUR>>) =
  combineFilter(other, exclusionFunction())
