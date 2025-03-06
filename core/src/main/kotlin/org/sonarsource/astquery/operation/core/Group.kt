package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.nodes.Combine
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.Unscope
import org.sonarsource.astquery.operation.Operation1to1

class GroupWithScopeOperation<FROM, GROUPED, TO>(
  val groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  val grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>,
) : Operation1to1<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    val scope = Scope(parent)
    val group = groupProducer(SingleBuilder(scope)).toSingle()
    val combine = Combine(scope, group.irNode, grouping)
    return Unscope(combine, setOf(scope))
  }
}

fun <FROM, GROUPED, TO> SingleBuilder<FROM>.groupWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>
): SingleBuilder<TO> {
  return apply(GroupWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> OptionalBuilder<FROM>.groupWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>
): OptionalBuilder<TO> {
  return apply(GroupWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> ManyBuilder<FROM>.groupWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>
): ManyBuilder<TO> {
  return apply(GroupWithScopeOperation(groupProducer, grouping))
}

private fun <A, B> pairFunction() = IdentifiedLambda<(A, B) -> Pair<A, B>>("pair", "Pair", ::Pair)

fun <FROM, GROUPED> SingleBuilder<FROM>.groupWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>
): SingleBuilder<Pair<FROM, GROUPED>> = groupWith(groupProducer, pairFunction())

fun <FROM, GROUPED> OptionalBuilder<FROM>.groupWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>
): OptionalBuilder<Pair<FROM, GROUPED>> = groupWith(groupProducer, pairFunction())

fun <FROM, GROUPED> ManyBuilder<FROM>.groupWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>
): ManyBuilder<Pair<FROM, GROUPED>> = groupWith(groupProducer, pairFunction())
