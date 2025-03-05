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

package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.core.combine
import org.sonarsource.astquery.operation.core.map

private fun <T> constantComparator(constant: T, comparator: IdentifiedLambda<(T, T) -> Boolean>) =
  IdentifiedLambda(
    id = comparator.id + ".constant($constant)",
    desc = comparator.desc,
    function = { const: T -> comparator.function(const, constant) }
  )

fun <CUR> SingleSelector<CUR>.compareConst(
  constant: CUR,
  comparator: IdentifiedLambda<(CUR, CUR) -> Boolean>
): SingleSelector<Boolean> = map(constantComparator(constant, comparator))

fun <CUR> OptionalSelector<CUR>.compareConst(
  constant: CUR,
  comparator: IdentifiedLambda<(CUR, CUR) -> Boolean>
): OptionalSelector<Boolean> = map(constantComparator(constant, comparator))

fun <CUR> ManySelector<CUR>.compareConst(
  constant: CUR,
  comparator: IdentifiedLambda<(CUR, CUR) -> Boolean>
): ManySelector<Boolean> = map(constantComparator(constant, comparator))

private val equalsFunction = IdentifiedLambda("equals", "Equals") { a: Any?, b: Any? -> a == b }
infix fun <CUR> SingleSelector<CUR>.eq(other: SingleSelector<CUR>) = combine(other, equalsFunction)
infix fun <CUR> OptionalSelector<CUR>.eq(other: SingleSelector<CUR>) = combine(other, equalsFunction)
infix fun <CUR> ManySelector<CUR>.eq(other: SingleSelector<CUR>) = combine(other, equalsFunction)

infix fun <CUR> SingleSelector<CUR>.eq(constant: CUR) = compareConst(constant, equalsFunction)
infix fun <CUR> OptionalSelector<CUR>.eq(constant: CUR) = compareConst(constant, equalsFunction)
infix fun <CUR> ManySelector<CUR>.eq(constant: CUR) = compareConst(constant, equalsFunction)

private val greaterFunction = IdentifiedLambda("greater", "Greater") { a: Int, b: Int -> a > b }
infix fun SingleSelector<Int>.gr(other: SingleSelector<Int>) = combine(other, greaterFunction)
infix fun OptionalSelector<Int>.gr(other: SingleSelector<Int>) = combine(other, greaterFunction)
infix fun ManySelector<Int>.gr(other: SingleSelector<Int>) = combine(other, greaterFunction)

infix fun SingleSelector<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)
infix fun OptionalSelector<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)
infix fun ManySelector<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)

