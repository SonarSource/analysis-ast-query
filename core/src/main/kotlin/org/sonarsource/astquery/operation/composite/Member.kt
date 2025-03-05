package org.sonar.plugins.java.api.query.operation.composite

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedFunction
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedFunction.Companion.fromMember
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.operation.core.flatMap
import org.sonar.plugins.java.api.query.operation.core.flatMapSeq
import org.sonar.plugins.java.api.query.operation.core.map
import org.sonar.plugins.java.api.query.operation.core.mapNonNull
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

// ========================== Simple Member Mapping ==========================
inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.property(prop: KProperty1<FROM, TO>) =
  map(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.property(prop: KProperty1<FROM, TO>) =
  map(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.property(prop: KProperty1<FROM, TO>) =
  map(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.func(function: KFunction1<FROM, TO>) =
  map(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.func(function: KFunction1<FROM, TO>) =
  map(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.func(function: KFunction1<FROM, TO>) =
  map(fromMember(FROM::class, function))

// ========================== Option Member Mapping ==========================

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.optProperty(prop: KProperty1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.optProperty(prop: KProperty1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.optProperty(prop: KProperty1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.optFunc(function: KFunction1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.optFunc(function: KFunction1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.optFunc(function: KFunction1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, function))

// ========================== List Member Mapping ==========================

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.listProperty(prop: KProperty1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.listProperty(prop: KProperty1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.listProperty(prop: KProperty1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.listFunc(function: KFunction1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.listFunc(function: KFunction1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.listFunc(function: KFunction1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, function))

// ========================== Nullable List Member Mapping ==========================

fun <T> optListToValues(): IdentifiedFunction<(Collection<T>?) -> Sequence<T>> =
  IdentifiedLambda("optional.to.values", "Values") { it?.asSequence() ?: emptySequence() }

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.optListProperty(prop: KProperty1<FROM, Collection<TO>?>) =
  property(prop).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.optListProperty(prop: KProperty1<FROM, Collection<TO>?>) =
  property(prop).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.optListProperty(prop: KProperty1<FROM, Collection<TO>?>) =
  property(prop).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> SingleSelector<out FROM>.optListFunc(function: KFunction1<FROM, Collection<TO>?>) =
  func(function).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> OptionalSelector<out FROM>.optListFunc(function: KFunction1<FROM, Collection<TO>?>) =
  func(function).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> ManySelector<out FROM>.optListFunc(function: KFunction1<FROM, Collection<TO>?>) =
  func(function).flatMapSeq(optListToValues())
