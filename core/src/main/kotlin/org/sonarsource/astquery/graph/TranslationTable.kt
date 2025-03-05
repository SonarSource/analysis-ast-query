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