package org.sonar.plugins.java.api.query.operation.core

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.Selector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.GraphUtils
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.ParentNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.Scope
import org.sonar.plugins.java.api.query.graph.ir.nodes.UnScope
import org.sonar.plugins.java.api.query.operation.Operation1to1
import org.sonar.plugins.java.api.query.operation.Operation1toN
import org.sonar.plugins.java.api.query.operation.Operation1toOptional

typealias Single<CUR> = SingleSelector<CUR>
typealias Option<CUR> = OptionalSelector<CUR>
typealias Many<CUR> = ManySelector<CUR>

private fun <CUR, TO> createScope(
  from: ParentNode<CUR>,
  builder: (Single<CUR>) -> Selector<TO, *>
): IRNode<*, TO> {
  val builderInput = Single(from)
  val end = builder(builderInput)

  val scope = Scope(from)
  val unscope = UnScope(end.current, scope)

  GraphUtils.copySubTree(from, scope, unscope)

  return unscope
}

class ScopeOperation1to1<CUR, TO>(val builder: (Single<CUR>) -> Selector<TO, *>) : Operation1to1<CUR, TO> {
  override fun applyTo(parent: ParentNode<CUR>): IRNode<*, out TO> {
    return createScope(parent, builder)
  }
}

class ScopeOperation1toOpt<CUR, TO>(val builder: (Single<CUR>) -> Selector<TO, *>) : Operation1toOptional<CUR, TO> {
  override fun applyTo(parent: ParentNode<CUR>): IRNode<*, out TO> {
    return createScope(parent, builder)
  }
}

class ScopeOperation1toN<CUR, TO>(val builder: (Single<CUR>) -> Selector<TO, *>) : Operation1toN<CUR, TO> {
  override fun applyTo(parent: ParentNode<CUR>): IRNode<*, out TO> {
    return createScope(parent, builder)
  }
}

fun <CUR, TO> Single<CUR>.scopedSingle(builder: (Single<CUR>) -> Selector<TO, TO>): Single<TO> =
  apply(ScopeOperation1to1(builder))

fun <CUR, TO> Single<CUR>.scopedOption(builder: (Single<CUR>) -> Option<TO>): Option<TO> =
  apply(ScopeOperation1toOpt(builder))

fun <CUR, TO> Single<CUR>.scoped(builder: (Single<CUR>) -> Selector<TO, *>): Many<TO> =
  apply(ScopeOperation1toN(builder))

fun <CUR, TO> Option<CUR>.scopedSingle(builder: (Single<CUR>) -> Single<TO>): Option<TO> =
  apply(ScopeOperation1to1(builder))

fun <CUR, TO> Option<CUR>.scopedOption(builder: (Single<CUR>) -> Option<TO>): Option<TO> =
  apply(ScopeOperation1toOpt(builder))

fun <CUR, TO> Option<CUR>.scoped(builder: (Single<CUR>) -> Selector<TO, *>): Many<TO> =
  apply(ScopeOperation1toN(builder))

fun <CUR, TO> Many<CUR>.scoped(builder: (Single<CUR>) -> Selector<TO, *>): Many<TO> =
  apply(ScopeOperation1toN(builder))

fun <CUR> Single<CUR>.scopeTo(from: Single<*>) = from.scopedSingle { this }
fun <CUR> Single<CUR>.scopeTo(from: Option<*>) = from.scopedSingle { this }
fun <CUR> Single<CUR>.scopeTo(from: Many<*>) = from.scoped { this }

fun <CUR> Option<CUR>.scopeTo(from: Single<*>) = from.scopedOption { this }
fun <CUR> Option<CUR>.scopeTo(from: Option<*>) = from.scopedOption { this }
fun <CUR> Option<CUR>.scopeTo(from: Many<*>) = from.scoped { this }

fun <CUR> Many<CUR>.scopeTo(from: Single<*>) = from.scoped { this }
fun <CUR> Many<CUR>.scopeTo(from: Option<*>) = from.scoped { this }
fun <CUR> Many<CUR>.scopeTo(from: Many<*>) = from.scoped { this }

