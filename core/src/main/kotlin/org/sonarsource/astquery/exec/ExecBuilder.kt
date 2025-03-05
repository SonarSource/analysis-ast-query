package org.sonarsource.astquery.exec

import org.sonarsource.astquery.Query
import org.sonarsource.astquery.operation.builder.Selector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.graph.GraphUtils
import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.graph.NodeId
import org.sonarsource.astquery.graph.TranslationTable
import org.sonarsource.astquery.ir.nodes.ChildNode
import org.sonarsource.astquery.ir.nodes.Combine
import org.sonarsource.astquery.ir.nodes.CombineDrop
import org.sonarsource.astquery.ir.nodes.Consumer
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.Root
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.UnScope

abstract class ExecBuilder<N : Node<N>> {

  abstract fun <IN> build(root: Root<IN>): Executable<IN>

  protected fun addScopesToCombines(root: Root<*>) {
    val applied = mutableSetOf<NodeId>()

    while (true) {
      val combine = GraphUtils.topologicalSort(root)
        .filter { it is Combine<*, *, *> || it is CombineDrop<*, *, *> }
        .reversed()
        .firstOrNull { applied.contains(it.id).not() }
        ?: return

      val dominator = combine.immediateDominator

      scope(combine, dominator)
      GraphUtils.removeDeadBranches(root)

      applied += combine.id
    }
  }

  private fun <OUT> scope(scoped: IRNode<*, *>, from: IRNode<*, OUT>) {
    val scope = Scope(from)
    GraphUtils.copySubTree(from, scope, scoped)

    val children = scoped.children
    val unscope = UnScope(scoped, scope)
    val table = TranslationTable()
    table.addParent(scoped, unscope)

    children.forEach { it.applyTranslation(table) }
  }

  private data class TangledScope(val mainScope: Scope<*>, val mainUnscope: UnScope<*>, val tangledScope: Scope<*>)

  protected fun untangleScopes(root: Root<*>) {
    while (true) {
      val nextTangledScope = findTangledScope(root) ?: return

      GraphUtils.removeNodeAndStitch(nextTangledScope.mainUnscope)
      nextTangledScope.tangledScope.unscopes.forEach {
        it.addScopeStart(nextTangledScope.mainScope)
      }
    }
  }

  private fun findTangledScope(root: Root<*>): TangledScope? {
    val scopeCandidates = GraphUtils.topologicalSort(root)
      .filterIsInstance<Scope<*>>()

    for (candidate in scopeCandidates) {
      for (unscope in candidate.unscopes) {
        val scopedNodes = candidate.strictDescendants
          .intersect(unscope.strictAncestors)

        // A tangled scope is any scoped scope that has an unscoped node that is outside the current scope
        val tangledScope = scopedNodes
          .filterIsInstance<Scope<*>>()
          .firstOrNull { s -> s.unscopes.any { it !in scopedNodes } }

        if (tangledScope != null) {
          return TangledScope(candidate, unscope, tangledScope)
        }
      }
    }

    return null
  }

  protected fun <T> applyMergeOptimization(node: IRNode<*, T>) {
    fun <OUT> tryToMerge(index: Int, child: IRNode<*, OUT>): Boolean {
      val candidates = node.children
        .filterIndexed { i, other -> i > index && other.canMergeWith(child) }
        .toList()

      candidates.forEach { candidate ->
        @Suppress("UNCHECKED_CAST")
        candidate.children.forEach { c ->
          child.addChild(c as ChildNode<OUT>)
        }

        candidate.delete()
      }

      // Return true if any candidates were merged
      return candidates.isNotEmpty()
    }

    fun <T> mergeEquivalentChildren(node: IRNode<*, T>) {
      while (true) {
        var merged = false
        for (i in node.children.indices) {
          if (tryToMerge(i, node.children[i])) {
            merged = true
            break
          }
        }

        if (!merged) {
          return
        }
      }
    }

    mergeEquivalentChildren(node)
    node.children.forEach { applyMergeOptimization(it) }
  }

  fun <INPUT, OUTPUT> createQuery(operations: (SingleSelector<INPUT>) -> Selector<*, OUTPUT>): Query<INPUT, OUTPUT> {
    val root = Root<INPUT>()
    val selector = SingleSelector(root)
    val output = operations(selector)

    return buildQuery(root, output)
  }

  private fun <IN, END, OUT> buildQuery(root: Root<IN>, end: Selector<END, OUT>): Query<IN, OUT> {

    val table = GraphUtils.copyTree(root)
    val root = table.get(root) as Root<IN>
    val endNode = table.get(end.irNode)

    val collector = Store { mutableListOf<END>() }
    // Create the Consumer node that will collect the results
    Consumer(endNode) { ctx, value ->
        collector.get(ctx).add(value)
    }

    val graph = build(root)

    return Query { ctx, input ->
        graph.execute(ctx, input)

        collector.get(ctx).let(end::toOutput)
    }
  }
}