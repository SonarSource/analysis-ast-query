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

package org.sonar.plugins.java.api.query

import org.sonar.plugins.java.api.query.graph.ir.identity
import org.sonar.plugins.java.api.query.graph.ir.nodes.Aggregate
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.operation.*

class ManySelector<CUR>(
  current: IRNode<*, out CUR>
) : Selector<CUR, List<CUR>>(current) {

  fun <TO> apply(op: Operation1to1<in CUR, TO>): ManySelector<TO> =
    ManySelector(op.applyTo(current))

  fun <TO> apply(op: Operation1toOptional<in CUR, TO>): ManySelector<TO> =
    ManySelector(op.applyTo(current))

  fun <TO> apply(op: OperationNto1<in CUR, TO>): SingleSelector<TO> =
    SingleSelector(op.applyTo(current))

  fun <TO> apply(op: OperationNtoOptional<in CUR, TO>): OptionalSelector<TO> =
    OptionalSelector(op.applyTo(current))

  override fun toOutput(current: List<CUR>): List<CUR> = current.toList()

  override fun toSingle(): SingleSelector<List<CUR>> = SingleSelector(Aggregate(current, identity("toSingle")))
}
