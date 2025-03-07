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

import org.sonarsource.astquery.ir.ExistFunction
import org.sonarsource.astquery.ir.NotExistFunction
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.core.aggregate

fun <CUR> OptionalBuilder<CUR>.isPresent(): SingleBuilder<Boolean> =
  aggregate(ExistFunction)

fun <CUR> ManyBuilder<CUR>.exists(): SingleBuilder<Boolean>  =
  aggregate(ExistFunction)

fun <CUR> OptionalBuilder<CUR>.notPresent(): SingleBuilder<Boolean>  =
  aggregate(NotExistFunction)

fun <CUR> ManyBuilder<CUR>.noneExists(): SingleBuilder<Boolean>  =
  aggregate(NotExistFunction)
