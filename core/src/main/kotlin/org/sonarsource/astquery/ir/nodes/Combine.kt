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

package org.sonarsource.astquery.ir.nodes

import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.graph.TranslationTable
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo

class Combine<LT, RT, OUT>(
  var left: ParentNode<LT>,
  var right: ParentNode<RT>,
  val combineFunc: IdentifiedFunction<(LT, RT) -> OUT>,
) : IRNode<Any?, OUT>(setOf(left, right)) {

  override val isSink = false

  override fun canMergeWith(other: IRNode<*, *>): Boolean =
    other is Combine<*, *, *> &&
      left == other.left &&
      right == other.right &&
      combineFunc == other.combineFunc

  override fun copy() = Combine(left, right, combineFunc)

  override fun applyTranslation(table: TranslationTable) {
    left = table.getParent(left)
    right = table.getParent(right)

    super.applyTranslation(table)
  }

  override fun getFlowType(parentsInfo: Map<IRNode<*, *>, VisualInfo>): FlowType {
    val leftFlow = parentsInfo[left]?.flowType ?: return FlowType.ERR
    val rightFlow = parentsInfo[right]?.flowType ?: return FlowType.ERR

    return leftFlow + rightFlow
  }

  override fun toString() = "Combine([${left.id}-${right.id}]${combineFunc.name})"
}
