package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.Selector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.nodes.Combine
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.Unscope
import org.sonarsource.astquery.operation.Operation1to1

class GroupWithScopeOperation<FROM, GROUPED, TO>(
  val groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  val grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>,
) : Operation1to1<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    val scope = Scope(parent)
    val group = groupProducer(SingleSelector(scope)).toSingle()
    val combine = Combine(scope, group.irNode, grouping)
    return Unscope(combine, setOf(scope))
  }
}

fun <FROM, GROUPED, TO> SingleSelector<FROM>.groupWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>
): SingleSelector<TO> {
  return apply(GroupWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> OptionalSelector<FROM>.groupWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>
): OptionalSelector<TO> {
  return apply(GroupWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> ManySelector<FROM>.groupWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>
): ManySelector<TO> {
  return apply(GroupWithScopeOperation(groupProducer, grouping))
}

private fun <A, B> pairFunction() = IdentifiedLambda<(A, B) -> Pair<A, B>>("pair", "Pair", ::Pair)

fun <FROM, GROUPED> SingleSelector<FROM>.groupWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>
): SingleSelector<Pair<FROM, GROUPED>> = groupWith(groupProducer, pairFunction())

fun <FROM, GROUPED> OptionalSelector<FROM>.groupWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>
): OptionalSelector<Pair<FROM, GROUPED>> = groupWith(groupProducer, pairFunction())

fun <FROM, GROUPED> ManySelector<FROM>.groupWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>
): ManySelector<Pair<FROM, GROUPED>> = groupWith(groupProducer, pairFunction())
