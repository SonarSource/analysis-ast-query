package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedFunction.Companion.fromMember
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.core.flatMap
import org.sonarsource.astquery.operation.core.flatMapSeq
import org.sonarsource.astquery.operation.core.map
import org.sonarsource.astquery.operation.core.mapNonNull
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

// ========================== Simple Member Mapping ==========================
inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.property(prop: KProperty1<FROM, TO>) =
  map(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.property(prop: KProperty1<FROM, TO>) =
  map(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.property(prop: KProperty1<FROM, TO>) =
  map(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.func(function: KFunction1<FROM, TO>) =
  map(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.func(function: KFunction1<FROM, TO>) =
  map(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.func(function: KFunction1<FROM, TO>) =
  map(fromMember(FROM::class, function))

// ========================== Option Member Mapping ==========================

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.optProperty(prop: KProperty1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.optProperty(prop: KProperty1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.optProperty(prop: KProperty1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.optFunc(function: KFunction1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.optFunc(function: KFunction1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.optFunc(function: KFunction1<FROM, TO>) =
  mapNonNull(fromMember(FROM::class, function))

// ========================== List Member Mapping ==========================

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.listProperty(prop: KProperty1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.listProperty(prop: KProperty1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.listProperty(prop: KProperty1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, prop))

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.listFunc(function: KFunction1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.listFunc(function: KFunction1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, function))

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.listFunc(function: KFunction1<FROM, Collection<TO>>) =
  flatMap(fromMember(FROM::class, function))

// ========================== Nullable List Member Mapping ==========================

fun <T> optListToValues(): IdentifiedFunction<(Collection<T>?) -> Sequence<T>> =
  IdentifiedLambda("optional.to.values", "Values") { it?.asSequence() ?: emptySequence() }

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.optListProperty(prop: KProperty1<FROM, Collection<TO>?>) =
  property(prop).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.optListProperty(prop: KProperty1<FROM, Collection<TO>?>) =
  property(prop).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.optListProperty(prop: KProperty1<FROM, Collection<TO>?>) =
  property(prop).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> SingleBuilder<out FROM>.optListFunc(function: KFunction1<FROM, Collection<TO>?>) =
  func(function).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> OptionalBuilder<out FROM>.optListFunc(function: KFunction1<FROM, Collection<TO>?>) =
  func(function).flatMapSeq(optListToValues())

inline fun <reified FROM : Any, TO> ManyBuilder<out FROM>.optListFunc(function: KFunction1<FROM, Collection<TO>?>) =
  func(function).flatMapSeq(optListToValues())
