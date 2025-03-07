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
package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.core.combine
import org.sonarsource.astquery.operation.core.map

private fun <T> constantComparator(constant: T, comparator: IdentifiedLambda<(T, T) -> Boolean>) =
  IdentifiedLambda(
    id = comparator.id + ".constant($constant)",
    desc = comparator.desc,
    function = { const: T -> comparator.function(const, constant) }
  )

fun <CUR> SingleBuilder<CUR>.compareConst(
  constant: CUR,
  comparator: IdentifiedLambda<(CUR, CUR) -> Boolean>
): SingleBuilder<Boolean> = map(constantComparator(constant, comparator))

fun <CUR> OptionalBuilder<CUR>.compareConst(
  constant: CUR,
  comparator: IdentifiedLambda<(CUR, CUR) -> Boolean>
): OptionalBuilder<Boolean> = map(constantComparator(constant, comparator))

fun <CUR> ManyBuilder<CUR>.compareConst(
  constant: CUR,
  comparator: IdentifiedLambda<(CUR, CUR) -> Boolean>
): ManyBuilder<Boolean> = map(constantComparator(constant, comparator))

private val equalsFunction = IdentifiedLambda("equals", "Equals") { a: Any?, b: Any? -> a == b }
infix fun <CUR> SingleBuilder<CUR>.eq(other: SingleBuilder<CUR>) = combine(other, equalsFunction)
infix fun <CUR> OptionalBuilder<CUR>.eq(other: SingleBuilder<CUR>) = combine(other, equalsFunction)
infix fun <CUR> ManyBuilder<CUR>.eq(other: SingleBuilder<CUR>) = combine(other, equalsFunction)

infix fun <CUR> SingleBuilder<CUR>.eq(constant: CUR) = compareConst(constant, equalsFunction)
infix fun <CUR> OptionalBuilder<CUR>.eq(constant: CUR) = compareConst(constant, equalsFunction)
infix fun <CUR> ManyBuilder<CUR>.eq(constant: CUR) = compareConst(constant, equalsFunction)

private val greaterFunction = IdentifiedLambda("greater", "Greater") { a: Int, b: Int -> a > b }
infix fun SingleBuilder<Int>.gr(other: SingleBuilder<Int>) = combine(other, greaterFunction)
infix fun OptionalBuilder<Int>.gr(other: SingleBuilder<Int>) = combine(other, greaterFunction)
infix fun ManyBuilder<Int>.gr(other: SingleBuilder<Int>) = combine(other, greaterFunction)

infix fun SingleBuilder<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)
infix fun OptionalBuilder<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)
infix fun ManyBuilder<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)

