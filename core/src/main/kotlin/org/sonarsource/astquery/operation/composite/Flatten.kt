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

import org.sonarsource.astquery.ir.identity
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.core.flatMap
import org.sonarsource.astquery.operation.core.flatMapSeq


private fun <T> flattenFunction() = identity<T>("Flatten")

fun <CUR> PipelineBuilder<out Collection<CUR>, *>.flatten() = flatMap(flattenFunction())
@JvmName("flatten-seq")
fun <CUR> PipelineBuilder<Sequence<CUR>, *>.flatten() = flatMapSeq(flattenFunction())
