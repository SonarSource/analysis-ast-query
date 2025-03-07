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
package org.sonar.plugins.java.api.query.operation.composite

import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.Droppable.Drop
import org.sonarsource.astquery.operation.Droppable.Keep
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.operation.composite.flatten
import org.sonarsource.astquery.operation.core.*

fun <T1, T2> ifPresentUse() = IdentifiedLambda("ifPresentUse", "IfPresentUse") { value: T1, other: List<T2> -> if (other.isNotEmpty()) Keep(value) else Drop }

fun <CUR, OTHER> OptionalBuilder<CUR>.ifPresentUse(other: SingleBuilder<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> OptionalBuilder<CUR>.ifPresentUse(other: OptionalBuilder<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> OptionalBuilder<CUR>.ifPresentUse(other: ManyBuilder<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> ManyBuilder<CUR>.ifPresentUse(other: SingleBuilder<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> ManyBuilder<CUR>.ifPresentUse(other: OptionalBuilder<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> ManyBuilder<CUR>.ifPresentUse(other: ManyBuilder<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <T> ifTrueUse() = IdentifiedLambda("ifTrueUse", "IfTrueUse") { value: T, condition: Boolean -> if (condition) Keep(value) else Drop }

fun <OTHER> SingleBuilder<Boolean>.ifTrueUse(other: SingleBuilder<OTHER>) =
  other.combineFilter(this, ifTrueUse())

fun <OTHER> SingleBuilder<Boolean>.ifTrueUse(other: OptionalBuilder<OTHER>) =
  other.combineFilter(this, ifTrueUse())

fun <OTHER> SingleBuilder<Boolean>.ifTrueUse(other: ManyBuilder<OTHER>) =
  other.combineFilter(this, ifTrueUse())

fun <T> orElseUse() = IdentifiedLambda("orElseUse", "OrElseUse") { orElse: T, value: T? -> value ?: orElse }
fun <T> ifNoneExistsUse() = IdentifiedLambda("ifNonExistsUse", "IfNonExistsUse") { first: List<T>, other: List<T> -> if (first.isNotEmpty()) first else other }
fun <T> singleton() = IdentifiedLambda("singleton", "Singleton") { value: T -> listOf(value) }

fun <T> OptionalBuilder<out T>.orElseUse(other: SingleBuilder<out T>): SingleBuilder<T> =
  other.combine(this.toSingle(), orElseUse())

fun <T> OptionalBuilder<out T>.orElseUse(other: OptionalBuilder<out T>): OptionalBuilder<T> =
  other.toSingle().combine(this.toSingle(), orElseUse()).filterNonNull()

fun <T> OptionalBuilder<out T>.orElseUse(other: ManyBuilder<out T>): ManyBuilder<T> =
  other.aggregate().combine(this.aggregate(), ifNoneExistsUse()).flatten()

fun <T> ManyBuilder<out T>.ifNoneExistsUse(other: SingleBuilder<out T>): ManyBuilder<T> =
  other.map(singleton()).combine(this.aggregate(), ifNoneExistsUse()).flatten()

fun <T> ManyBuilder<out T>.ifNoneExistsUse(other: OptionalBuilder<out T>): ManyBuilder<T> =
  other.aggregate().combine(this.aggregate(), ifNoneExistsUse()).flatten()

fun <T> ManyBuilder<out T>.ifNoneExistsUse(other: ManyBuilder<out T>): ManyBuilder<T> =
  other.aggregate().combine(this.aggregate(), ifNoneExistsUse()).flatten()
