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
package org.sonarsource.astquery.graph

import org.sonarsource.astquery.ir.nodes.ChildNode
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode

class TranslationTable {

  private val table = mutableMapOf<IRNode<*, *>, IRNode<*, *>>()

  fun <IN, OUT> add(original: IRNode<IN, OUT>, translated: IRNode<IN, OUT>) {
    table[original] = translated
  }

  fun <OUT> addParent(original: ParentNode<OUT>, translated: ParentNode<OUT>) {
    table[original] = translated
  }

  fun <IN> addChild(original: ChildNode<IN>, translated: ChildNode<IN>) {
    table[original] = translated
  }

  fun hasTranslation(original: IRNode<*, *>): Boolean = table.containsKey(original)

  @Suppress("UNCHECKED_CAST")
  fun <IN, OUT> get(original: IRNode<IN, OUT>) = (table[original] as IRNode<IN, OUT>?) ?: original

  fun <OUT> getParent(original: ParentNode<OUT>): ParentNode<OUT> = get(original)

  fun <IN> getChild(original: ChildNode<IN>): ChildNode<IN> = get(original)

  fun remove(node: IRNode<*, *>) = table.remove(node)
}