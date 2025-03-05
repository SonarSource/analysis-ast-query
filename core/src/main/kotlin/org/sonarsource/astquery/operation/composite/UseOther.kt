package org.sonar.plugins.java.api.query.operation.composite

import org.sonarsource.astquery.operation.Droppable.Drop
import org.sonarsource.astquery.operation.Droppable.Keep
import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.operation.core.aggregate
import org.sonar.plugins.java.api.query.operation.core.combine
import org.sonar.plugins.java.api.query.operation.core.combineFilter
import org.sonar.plugins.java.api.query.operation.core.filterNonNull
import org.sonar.plugins.java.api.query.operation.core.map

fun <T1, T2> ifPresentUse() = IdentifiedLambda("ifPresentUse", "IfPresentUse") { value: T1, other: List<T2> -> if (other.isNotEmpty()) Keep(value) else Drop }

fun <CUR, OTHER> OptionalSelector<CUR>.ifPresentUse(other: SingleSelector<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> OptionalSelector<CUR>.ifPresentUse(other: OptionalSelector<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> OptionalSelector<CUR>.ifPresentUse(other: ManySelector<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> ManySelector<CUR>.ifPresentUse(other: SingleSelector<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> ManySelector<CUR>.ifPresentUse(other: OptionalSelector<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <CUR, OTHER> ManySelector<CUR>.ifPresentUse(other: ManySelector<OTHER>) =
  other.combineFilter(this.aggregate(), ifPresentUse())

fun <T> ifTrueUse() = IdentifiedLambda("ifTrueUse", "IfTrueUse") { value: T, condition: Boolean -> if (condition) Keep(value) else Drop }

fun <OTHER> SingleSelector<Boolean>.ifTrueUse(other: SingleSelector<OTHER>) =
  other.combineFilter(this, ifTrueUse())

fun <OTHER> SingleSelector<Boolean>.ifTrueUse(other: OptionalSelector<OTHER>) =
  other.combineFilter(this, ifTrueUse())

fun <OTHER> SingleSelector<Boolean>.ifTrueUse(other: ManySelector<OTHER>) =
  other.combineFilter(this, ifTrueUse())

fun <T> orElseUse() = IdentifiedLambda("orElseUse", "OrElseUse") { orElse: T, value: T? -> value ?: orElse }
fun <T> ifNoneExistsUse() = IdentifiedLambda("ifNonExistsUse", "IfNonExistsUse") { first: List<T>, other: List<T> -> if (first.isNotEmpty()) first else other }
fun <T> singleton() = IdentifiedLambda("singleton", "Singleton") { value: T -> listOf(value) }

fun <T> OptionalSelector<out T>.orElseUse(other: SingleSelector<out T>): SingleSelector<T> =
  other.combine(this.toSingle(), orElseUse())

fun <T> OptionalSelector<out T>.orElseUse(other: OptionalSelector<out T>): OptionalSelector<T> =
  other.toSingle().combine(this.toSingle(), orElseUse()).filterNonNull()

fun <T> OptionalSelector<out T>.orElseUse(other: ManySelector<out T>): ManySelector<T> =
  other.aggregate().combine(this.aggregate(), ifNoneExistsUse()).flatten()

fun <T> ManySelector<out T>.ifNoneExistsUse(other: SingleSelector<out T>): ManySelector<T> =
  other.map(singleton()).combine(this.aggregate(), ifNoneExistsUse()).flatten()

fun <T> ManySelector<out T>.ifNoneExistsUse(other: OptionalSelector<out T>): ManySelector<T> =
  other.aggregate().combine(this.aggregate(), ifNoneExistsUse()).flatten()

fun <T> ManySelector<out T>.ifNoneExistsUse(other: ManySelector<out T>): ManySelector<T> =
  other.aggregate().combine(this.aggregate(), ifNoneExistsUse()).flatten()
