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

package org.sonar.plugins.java.api.query.operation.core

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.exec.ExecutionContext
import org.sonar.plugins.java.api.query.graph.ir.nodes.Consumer
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.ParentNode
import org.sonar.plugins.java.api.query.operation.Operation1to1

class ConsumeOperation<C>(
  private val consume: (ExecutionContext, C) -> Unit
) : Operation1to1<C, C> {

  override fun applyTo(parent: ParentNode<C>): IRNode<*, out C> {
    return Consumer(parent, consume)
  }
}
fun <C> SingleSelector<C>.consume(reporter: (ExecutionContext, C) -> Unit): SingleSelector<C> {
  return apply(ConsumeOperation(reporter))
}

fun <C> OptionalSelector<C>.consume(reporter: (ExecutionContext, C) -> Unit): OptionalSelector<C> {
  return apply(ConsumeOperation(reporter))
}

fun <C> ManySelector<C>.consume(reporter: (ExecutionContext, C) -> Unit): ManySelector<C> {
  return apply(ConsumeOperation(reporter))
}
