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
package org.sonarsource.astquery.ir.nodes

import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.TranslationTable
import org.sonarsource.astquery.graph.visual.FlowType
import org.sonarsource.astquery.graph.visual.VisualInfo
import org.sonarsource.astquery.ir.ResettableLazy

typealias ParentNode<T> = IRNode<*, out T>
typealias ChildNode<T> = IRNode<in T, *>

var nextId = 0

sealed class IRNode<IN, OUT> : Node<IRNode<*, *>> {

  final override val id: NodeId = nextId++

  /** The children of the node. */
  final override var children: List<ChildNode<OUT>> = emptyList()
    private set(value) {
      field = value
      onChildChanged()
    }

  /** This set contains all the nodes whose result will be used by the current node. */
  var parents: Set<ParentNode<IN>> = emptySet()
    private set(value) {
      field = value
      onParentChanged()
    }

  constructor(parent: ParentNode<IN>) {
    addParent(parent)
  }

  constructor(parents: Set<ParentNode<IN>>) {
    parents.forEach(::addParent)
  }

  fun addChild(child: ChildNode<OUT>) {
    if (child in children) return

    children += child
    child.addParent(this)
  }

  fun removeChild(child: ChildNode<OUT>) {
    if (child !in children) return

    children -= child
    child.removeParent(this)
  }

  fun addParent(parent: ParentNode<IN>) {
    if (parent in parents) return

    parents += parent
    parent.addChild(this)
  }

  fun removeParent(parent: ParentNode<IN>) {
    if (parent !in parents) return

    parents -= parent
    parent.removeChild(this)
  }

  abstract fun canMergeWith(other: IRNode<*, *>): Boolean

  /** All ancestors of the node, including itself. */
  val ancestors: Set<IRNode<*, *>> get() = lazyAncestors.value

  /** All ancestors of the node, excluding itself. */
  val strictAncestors get() = ancestors - this
  private val lazyAncestors = ResettableLazy {
    if (parents.isEmpty()) setOf(this)
    else parents.flatMap { it.ancestors + this }.toSet()
  }

  /** All descendants of the node, including itself. */
  val descendants: Set<IRNode<*, *>> get() = lazyDescendants.value

  /** All descendants of the node, excluding itself. */
  val strictDescendants get() = descendants - this
  private val lazyDescendants = ResettableLazy {
    if (children.isEmpty()) setOf(this)
    else children.flatMap { it.descendants + this }.toSet()
  }

  /** All dominators of the node, including itself. */
  val dominators: Set<IRNode<*, *>> get() = lazyDominators.value

  /** All dominators of the node, excluding itself. */
  val strictDominators get() = dominators - this
  private val lazyDominators = ResettableLazy {
    if (parents.isEmpty()) setOf(this)
    else parents
      .map { it.dominators + it }
      .reduce { acc, cur -> acc.intersect(cur) }
  }

  /** The single immediate dominator of the node. It does not exist for the root node. */
  val immediateDominator: IRNode<*, *> get() = lazyImmediateDominator.value
  private val lazyImmediateDominator = ResettableLazy {
    // The immediate dominator is the only strict dominator which is not a dominator of the others
    // By definition, there is only one immediate dominator except for the root node
    val idom = strictDominators.singleOrNull { s ->
      strictDominators.minus(s).all { s !in it.dominators }
    }
    idom ?: throw IllegalStateException("Immediate dominator not present")
  }

  /** Remove the node from the graph. */
  open fun delete() {
    parents.forEach { it.removeChild(this) }
    children.forEach { it.removeParent(this) }
  }

  /** Is true if the node is on a path to an exit point of the execution. */
  val hasSink: Boolean get() = lazyHasSink.value
  private val lazyHasSink = ResettableLazy {
    isSink || children.any { it.hasSink }
  }

  private fun onParentChanged() {
    lazyAncestors.reset()
    lazyDominators.reset()
    lazyImmediateDominator.reset()
    children.forEach { it.onParentChanged() }
  }

  private fun onChildChanged() {
    lazyDescendants.reset()
    lazyHasSink.reset()
    parents.forEach { it.onChildChanged() }
  }

  /** Create a copy of the node given a [TranslationTable] of all ancestors (if translated), it will have no children. */
  abstract fun copy(): IRNode<IN, OUT>

  /** Move the node to the translated parents, it will keep its children. */
  open fun applyTranslation(table: TranslationTable) {
    val newParents = parents.map { table.getParent(it) }.toSet()
    parents.forEach { it.removeChild(this) }
    newParents.forEach { it.addChild(this) }
  }

  override fun getVisualizationInfo(parentsInfo: Map<IRNode<*, *>, VisualInfo>): VisualInfo {
    return VisualInfo(
      id = id.toString(),
      name = toString(),
      flowType = getFlowType(parentsInfo),
      secondaryLinks = secondaryVisualLinks(parentsInfo),
      color = if (!hasSink) "ff0000" else null
    )
  }

  abstract fun getFlowType(parentsInfo: Map<IRNode<*, *>, VisualInfo>): FlowType

  protected open fun secondaryVisualLinks(parentsInfo: Map<IRNode<*, *>, VisualInfo>): Set<String> = emptySet()

  protected fun getParentFlowType(parentsInfo: Map<IRNode<*, *>, VisualInfo>) =
    parentsInfo.getValue(parents.single()).flowType
}
