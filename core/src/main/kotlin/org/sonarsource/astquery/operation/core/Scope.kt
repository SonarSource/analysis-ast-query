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
package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.graph.GraphUtils
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.Unscope
import org.sonarsource.astquery.operation.Operation1to1
import org.sonarsource.astquery.operation.Operation1toN
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder

typealias Single<CUR> = SingleBuilder<CUR>
typealias Option<CUR> = OptionalBuilder<CUR>
typealias Many<CUR> = ManyBuilder<CUR>

private fun <CUR, TO> createScope(
  from: ParentNode<CUR>,
  builder: (Single<CUR>) -> PipelineBuilder<TO, *>
): IRNode<*, TO> {
  val builderInput = Single(from)
  val end = builder(builderInput)

  val scope = Scope(from)
  val unscope = Unscope(end.irNode, scope)

  GraphUtils.copySubTree(from, scope, unscope)

  return unscope
}

class ScopeOperation1to1<CUR, TO>(val builder: (Single<CUR>) -> PipelineBuilder<TO, *>) : Operation1to1<CUR, TO> {
  override fun applyTo(parent: ParentNode<CUR>): IRNode<*, out TO> {
    return createScope(parent, builder)
  }
}

class ScopeOperation1toOpt<CUR, TO>(val builder: (Single<CUR>) -> PipelineBuilder<TO, *>) : Operation1toOptional<CUR, TO> {
  override fun applyTo(parent: ParentNode<CUR>): IRNode<*, out TO> {
    return createScope(parent, builder)
  }
}

class ScopeOperation1toN<CUR, TO>(val builder: (Single<CUR>) -> PipelineBuilder<TO, *>) : Operation1toN<CUR, TO> {
  override fun applyTo(parent: ParentNode<CUR>): IRNode<*, out TO> {
    return createScope(parent, builder)
  }
}

fun <CUR, TO> Single<CUR>.scopedSingle(builder: (Single<CUR>) -> PipelineBuilder<TO, TO>): Single<TO> =
  apply(ScopeOperation1to1(builder))

fun <CUR, TO> Single<CUR>.scopedOption(builder: (Single<CUR>) -> Option<TO>): Option<TO> =
  apply(ScopeOperation1toOpt(builder))

fun <CUR, TO> Single<CUR>.scoped(builder: (Single<CUR>) -> PipelineBuilder<TO, *>): Many<TO> =
  apply(ScopeOperation1toN(builder))

fun <CUR, TO> Option<CUR>.scopedSingle(builder: (Single<CUR>) -> Single<TO>): Option<TO> =
  apply(ScopeOperation1to1(builder))

fun <CUR, TO> Option<CUR>.scopedOption(builder: (Single<CUR>) -> Option<TO>): Option<TO> =
  apply(ScopeOperation1toOpt(builder))

fun <CUR, TO> Option<CUR>.scoped(builder: (Single<CUR>) -> PipelineBuilder<TO, *>): Many<TO> =
  apply(ScopeOperation1toN(builder))

fun <CUR, TO> Many<CUR>.scoped(builder: (Single<CUR>) -> PipelineBuilder<TO, *>): Many<TO> =
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

