package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.core.combine


private fun <A, B> pairFunction() = IdentifiedLambda("pair", "Pair") { a: A, b: B -> Pair(a, b) }
infix fun <CUR, OTHER> SingleSelector<CUR>.zip(other: SingleSelector<OTHER>) = combine(other, pairFunction())
infix fun <CUR, OTHER> OptionalSelector<CUR>.zip(other: SingleSelector<OTHER>) = combine(other, pairFunction())
infix fun <CUR, OTHER> ManySelector<CUR>.zip(other: SingleSelector<OTHER>) = combine(other, pairFunction())
