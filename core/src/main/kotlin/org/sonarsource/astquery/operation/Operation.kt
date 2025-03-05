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

package org.sonar.plugins.java.api.query.operation

import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.graph.ir.OpId
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.ParentNode

fun interface Operation1to1<FROM, TO> {
  fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO>
}

fun interface Operation1toOptional<FROM, TO> {
  fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO>
}

fun interface Operation1toN<FROM, TO> {
  fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO>
}

fun interface OperationNto1<FROM, TO> {
  fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO>
}

fun interface OperationNtoOptional<FROM, TO> {
  fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO>
}

fun <FUNC : Function<*>> idFunction(id: OpId? = null, name: String? = null, lambda: FUNC) =
  IdentifiedLambda(id, name, lambda)
