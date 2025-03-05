package org.sonar.plugins.java.api.query.operation.core

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.Selector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedFunction
import org.sonar.plugins.java.api.query.graph.ir.IdentifiedLambda
import org.sonar.plugins.java.api.query.graph.ir.nodes.Combine
import org.sonar.plugins.java.api.query.graph.ir.nodes.IRNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.ParentNode
import org.sonar.plugins.java.api.query.graph.ir.nodes.Scope
import org.sonar.plugins.java.api.query.graph.ir.nodes.UnScope
import org.sonar.plugins.java.api.query.operation.Operation1to1

class GroupWithScopeOperation<FROM, GROUPED, TO>(
  val groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  val grouping: IdentifiedFunction<(FROM, GROUPED) -> TO>,
) : Operation1to1<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    val scope = Scope(parent)
    val group = groupProducer(SingleSelector(scope)).toSingle()
    val combine = Combine(scope, group.current, grouping)
    return UnScope(combine, setOf(scope))
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
