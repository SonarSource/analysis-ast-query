package org.sonarsource.astquery.operation.composite

import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.operation.core.combine


private fun <A, B> pairFunction() = IdentifiedLambda("pair", "Pair") { a: A, b: B -> Pair(a, b) }
infix fun <CUR, OTHER> SingleBuilder<CUR>.zip(other: SingleBuilder<OTHER>) = combine(other, pairFunction())
infix fun <CUR, OTHER> OptionalBuilder<CUR>.zip(other: SingleBuilder<OTHER>) = combine(other, pairFunction())
infix fun <CUR, OTHER> ManyBuilder<CUR>.zip(other: SingleBuilder<OTHER>) = combine(other, pairFunction())
