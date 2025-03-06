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

package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.ir.FirstFunction
import org.sonarsource.astquery.ir.FirstOrDefaultFunction
import org.sonarsource.astquery.operation.core.aggregate
import org.sonarsource.astquery.operation.core.aggregateFilter

fun <CUR> ManyBuilder<CUR>.first(): OptionalBuilder<CUR> =
  aggregateFilter(FirstFunction())

fun <OUT> OptionalBuilder<out OUT>.orElse(default: OUT): SingleBuilder<OUT> =
  aggregate(FirstOrDefaultFunction(default))

fun <CUR> OptionalBuilder<CUR>.orElseNull(): SingleBuilder<CUR?> =
  aggregate(FirstOrDefaultFunction(null))

fun <OUT> ManyBuilder<out OUT>.firstOrElse(default: OUT): SingleBuilder<OUT> =
  aggregate(FirstOrDefaultFunction(default))

fun <CUR> ManyBuilder<CUR>.firstOrNull(): SingleBuilder<CUR?> =
  aggregate(FirstOrDefaultFunction(null))
