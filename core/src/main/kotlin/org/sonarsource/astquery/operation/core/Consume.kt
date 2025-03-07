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
package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.ir.nodes.Consumer
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.operation.Operation1to1
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder

class ConsumeOperation<C>(
  private val consume: (ExecutionContext, C) -> Unit
) : Operation1to1<C, C> {

  override fun applyTo(parent: ParentNode<C>): IRNode<*, out C> {
    return Consumer(parent, consume)
  }
}
fun <C> SingleBuilder<C>.consume(reporter: (ExecutionContext, C) -> Unit): SingleBuilder<C> {
  return apply(ConsumeOperation(reporter))
}

fun <C> OptionalBuilder<C>.consume(reporter: (ExecutionContext, C) -> Unit): OptionalBuilder<C> {
  return apply(ConsumeOperation(reporter))
}

fun <C> ManyBuilder<C>.consume(reporter: (ExecutionContext, C) -> Unit): ManyBuilder<C> {
  return apply(ConsumeOperation(reporter))
}
