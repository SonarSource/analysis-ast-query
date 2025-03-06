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

package org.sonarsource.astquery.operation.builder

import org.sonarsource.astquery.ir.identity
import org.sonarsource.astquery.ir.nodes.Aggregate
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.operation.Operation1to1
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.OperationNto1
import org.sonarsource.astquery.operation.OperationNtoOptional

class ManyBuilder<CUR>(
  current: IRNode<*, out CUR>
) : PipelineBuilder<CUR, List<CUR>>(current) {

  fun <TO> apply(op: Operation1to1<in CUR, TO>): ManyBuilder<TO> =
    ManyBuilder(op.applyTo(irNode))

  fun <TO> apply(op: Operation1toOptional<in CUR, TO>): ManyBuilder<TO> =
    ManyBuilder(op.applyTo(irNode))

  fun <TO> apply(op: OperationNto1<in CUR, TO>): SingleBuilder<TO> =
    SingleBuilder(op.applyTo(irNode))

  fun <TO> apply(op: OperationNtoOptional<in CUR, TO>): OptionalBuilder<TO> =
    OptionalBuilder(op.applyTo(irNode))

  override fun toOutput(current: List<CUR>): List<CUR> = current.toList()

  override fun toSingle(): SingleBuilder<List<CUR>> = SingleBuilder(Aggregate(irNode, identity("toSingle")))
}
