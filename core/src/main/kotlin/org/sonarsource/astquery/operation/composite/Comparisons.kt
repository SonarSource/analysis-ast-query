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

package org.sonar.plugins.java.api.query.operation.composite

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.operation.core.combine
import org.sonar.plugins.java.api.query.operation.core.map

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
infix fun <INIT> SingleSelector<Int>.gr(other: SingleSelector<Int>) = combine(other, greaterFunction)
infix fun <INIT> OptionalSelector<Int>.gr(other: SingleSelector<Int>) = combine(other, greaterFunction)
infix fun <INIT> ManySelector<Int>.gr(other: SingleSelector<Int>) = combine(other, greaterFunction)

infix fun <INIT> SingleSelector<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)
infix fun <INIT> OptionalSelector<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)
infix fun <INIT> ManySelector<Int>.gr(constant: Int) = compareConst(constant, greaterFunction)

/* TODO Implement the rest of the comparison functions
infix fun <INIT> SingleSelector<Int>.gre(other: Selector<Int, *>) = compare(other) { a, b -> a >= b }
infix fun <INIT> OptionalSelector<Int>.gre(other: Selector<Int, *>) = compare(other) { a, b -> a >= b }
infix fun <INIT> ManySelector<Int>.gre(other: Selector<Int, *>) = compare(other) { a, b -> a >= b }

infix fun <INIT> SingleSelector<Int>.gre(constant: Int) = compareConst(constant) { a, b -> a >= b }
infix fun <INIT> OptionalSelector<Int>.gre(constant: Int) = compareConst(constant) { a, b -> a >= b }
infix fun <INIT> ManySelector<Int>.gre(constant: Int) = compareConst(constant) { a, b -> a >= b }

infix fun <INIT> SingleSelector<Int>.lr(other: Selector<Int, *>) = compare(other) { a, b -> a < b }
infix fun <INIT> OptionalSelector<Int>.lr(other: Selector<Int, *>) = compare(other) { a, b -> a < b }
infix fun <INIT> ManySelector<Int>.lr(other: Selector<Int, *>) = compare(other) { a, b -> a < b }

infix fun <INIT> SingleSelector<Int>.lr(constant: Int) = compareConst(constant) { a, b -> a < b }
infix fun <INIT> OptionalSelector<Int>.lr(constant: Int) = compareConst(constant) { a, b -> a < b }
infix fun <INIT> ManySelector<Int>.lr(constant: Int) = compareConst(constant) { a, b -> a < b }

infix fun <INIT> SingleSelector<Int>.lre(other: Selector<Int, *>) = compare(other) { a, b -> a <= b }
infix fun <INIT> OptionalSelector<Int>.lre(other: Selector<Int, *>) = compare(other) { a, b -> a <= b }
infix fun <INIT> ManySelector<Int>.lre(other: Selector<Int, *>) = compare(other) { a, b -> a <= b }

infix fun <INIT> SingleSelector<Int>.lre(constant: Int) = compareConst(constant) { a, b -> a <= b }
infix fun <INIT> OptionalSelector<Int>.lre(constant: Int) = compareConst(constant) { a, b -> a <= b }
infix fun <INIT> ManySelector<Int>.lre(constant: Int) = compareConst(constant) { a, b -> a <= b }
*/
