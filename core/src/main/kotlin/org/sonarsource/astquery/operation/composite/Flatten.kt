package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.operation.builder.Selector
import org.sonarsource.astquery.ir.identity
import org.sonarsource.astquery.operation.core.flatMap
import org.sonarsource.astquery.operation.core.flatMapSeq


private fun <T> flattenFunction() = identity<T>("Flatten")

fun <CUR> Selector<out Collection<CUR>, *>.flatten() = flatMap(flattenFunction())
@JvmName("flatten-seq")
fun <CUR> Selector<Sequence<CUR>, *>.flatten() = flatMapSeq(flattenFunction())
