package org.sonar.plugins.java.api.query.operation.composite

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.operation.core.combine


private fun <A, B> pairFunction() = IdentifiedLambda("pair", "Pair") { a: A, b: B -> Pair(a, b) }
infix fun <CUR, OTHER> SingleSelector<CUR>.zip(other: SingleSelector<OTHER>) = combine(other, pairFunction())
infix fun <CUR, OTHER> OptionalSelector<CUR>.zip(other: SingleSelector<OTHER>) = combine(other, pairFunction())
infix fun <CUR, OTHER> ManySelector<CUR>.zip(other: SingleSelector<OTHER>) = combine(other, pairFunction())
