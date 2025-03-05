package org.sonarsource.astquery.exec

import org.sonar.plugins.java.api.query.Query
import org.sonar.plugins.java.api.query.Selector
import org.sonar.plugins.java.api.query.graph.GraphUtils
import org.sonar.plugins.java.api.query.graph.Node
import org.sonar.plugins.java.api.query.graph.NodeId
import org.sonarsource.astquery.graph.TranslationTable
import org.sonar.plugins.java.api.query.graph.ir.nodes.ChildNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.Combine
import org.sonar.plugins.java.api.query.graph.ir.nodes.CombineDrop
import org.sonar.plugins.java.api.query.graph.ir.nodes.Consumer
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.Root
import org.sonar.plugins.java.api.query.graph.ir.nodes.Scope
import org.sonar.plugins.java.api.query.graph.ir.nodes.UnScope

abstract class Builder<N : Node<N>> {

  abstract fun <IN> build(root: Root<IN>): ExecutionGraph<N, IN>

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

  fun <IN, END, OUT> buildQuery(root: Root<IN>, end: Selector<END, OUT>): Query<IN, OUT> {
    return buildQuery(root, end.current, end::toOutput)
  }

  fun <IN, END, OUT> buildQuery(root: Root<IN>, end: IRNode<*, END>, transform: (List<END>) -> OUT): Query<IN, OUT> {

    val table = GraphUtils.copyTree(root)
    val root = table.get(root) as Root<IN>
    val end = table.get(end)

    val collector = Store { mutableListOf<END>() }
      Consumer(end) { ctx, value ->
          collector.get(ctx).add(value)
      }

    GraphUtils.removeDeadBranches(root)

    val graph = build(root)

    return Query { ctx, input ->
        val execCtx = ExecutionContext(ctx)
        graph.execute(execCtx, input)

        collector.get(execCtx).let(transform)
    }
  }
}