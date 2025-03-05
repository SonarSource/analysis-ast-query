package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.operation.Droppable
import org.sonarsource.astquery.operation.Droppable.Drop
import org.sonarsource.astquery.operation.Droppable.Keep
import org.sonarsource.astquery.operation.builder.ManySelector
import org.sonarsource.astquery.operation.builder.OptionalSelector
import org.sonarsource.astquery.operation.builder.Selector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.nodes.CombineDrop
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.UnScope
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.composite.orElse

class GroupFilterWithScopeOperation <FROM, GROUPED, TO>(
  val groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  val grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>,
) : Operation1toOptional<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    val scope = Scope(parent)
    val group = groupProducer(SingleSelector(scope)).toSingle()
    val combine = CombineDrop(scope, group.irNode, grouping)
    return UnScope(combine, setOf(scope))
  }
}

fun <FROM, GROUPED, TO> SingleSelector<FROM>.groupFilterWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): OptionalSelector<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> OptionalSelector<FROM>.groupFilterWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): OptionalSelector<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> ManySelector<FROM>.groupFilterWith(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): ManySelector<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

private fun <A, B> dropPairFunction() = IdentifiedLambda("dropPair", "DroppablePair") { from: A, drop: Droppable<B> ->
  if (drop is Keep) Keep(Pair(from, drop.data)) else Drop
}

private fun <T> keepFunction() = IdentifiedLambda("keep", "Keep") { value: T -> Keep(value) }
private fun <FROM, TO> toDroppable(func: (SingleSelector<FROM>) -> OptionalSelector<TO>) =
  { from: SingleSelector<FROM> -> func(from).map(keepFunction()).orElse(Drop) }

fun <FROM, TO> SingleSelector<FROM>.groupFilterWith(
  groupProducer: (SingleSelector<FROM>) -> OptionalSelector<TO>
): OptionalSelector<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

fun <FROM, TO> OptionalSelector<FROM>.groupFilterWith(
  groupProducer: (SingleSelector<FROM>) -> OptionalSelector<TO>
): OptionalSelector<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

fun <FROM, TO> ManySelector<FROM>.groupFilterWith(
  groupProducer: (SingleSelector<FROM>) -> OptionalSelector<TO>
): ManySelector<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

private fun <T> whereFunction() = IdentifiedLambda<(T, Boolean) -> Droppable<T>>("where", "Where") { value, keep ->
  if (keep) Keep(value) else Drop
}

fun <FROM> SingleSelector<FROM>.where(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, Boolean>
): OptionalSelector<FROM> = groupFilterWith(groupProducer, whereFunction())

fun <FROM> OptionalSelector<FROM>.where(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, Boolean>
): OptionalSelector<FROM> = groupFilterWith(groupProducer, whereFunction())

fun <FROM> ManySelector<FROM>.where(
  groupProducer: (SingleSelector<FROM>) -> Selector<*, Boolean>
): ManySelector<FROM> = groupFilterWith(groupProducer, whereFunction())
