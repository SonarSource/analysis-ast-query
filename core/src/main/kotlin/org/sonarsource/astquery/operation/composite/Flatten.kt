package org.sonar.plugins.java.api.query.operation.composite

import org.sonar.plugins.java.api.query.Selector
import org.sonar.plugins.java.api.query.graph.ir.identity
import org.sonar.plugins.java.api.query.operation.core.flatMap
import org.sonar.plugins.java.api.query.operation.core.flatMapSeq


private fun <T> flattenFunction() = identity<T>("Flatten")

fun <CUR> Selector<out Collection<CUR>, *>.flatten() = flatMap(flattenFunction())
@JvmName("flatten-seq")
fun <CUR> Selector<Sequence<CUR>, *>.flatten() = flatMapSeq(flattenFunction())
