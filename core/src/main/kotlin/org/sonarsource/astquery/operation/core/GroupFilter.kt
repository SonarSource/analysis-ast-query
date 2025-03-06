package org.sonarsource.astquery.operation.core

import org.sonarsource.astquery.operation.Droppable
import org.sonarsource.astquery.operation.Droppable.Drop
import org.sonarsource.astquery.operation.Droppable.Keep
import org.sonarsource.astquery.operation.builder.ManyBuilder
import org.sonarsource.astquery.operation.builder.OptionalBuilder
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.ir.IdentifiedFunction
import org.sonarsource.astquery.ir.IdentifiedLambda
import org.sonarsource.astquery.ir.nodes.CombineDrop
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.ParentNode
import org.sonarsource.astquery.ir.nodes.Scope
import org.sonarsource.astquery.ir.nodes.Unscope
import org.sonarsource.astquery.operation.Operation1toOptional
import org.sonarsource.astquery.operation.composite.orElse

class GroupFilterWithScopeOperation <FROM, GROUPED, TO>(
  val groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  val grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>,
) : Operation1toOptional<FROM, TO> {
  override fun applyTo(parent: ParentNode<FROM>): IRNode<*, out TO> {
    val scope = Scope(parent)
    val group = groupProducer(SingleBuilder(scope)).toSingle()
    val combine = CombineDrop(scope, group.irNode, grouping)
    return Unscope(combine, setOf(scope))
  }
}

fun <FROM, GROUPED, TO> SingleBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): OptionalBuilder<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> OptionalBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): OptionalBuilder<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

fun <FROM, GROUPED, TO> ManyBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, GROUPED>,
  grouping: IdentifiedFunction<(FROM, GROUPED) -> Droppable<TO>>
): ManyBuilder<TO> {
  return apply(GroupFilterWithScopeOperation(groupProducer, grouping))
}

private fun <A, B> dropPairFunction() = IdentifiedLambda("dropPair", "DroppablePair") { from: A, drop: Droppable<B> ->
  if (drop is Keep) Keep(Pair(from, drop.data)) else Drop
}

private fun <T> keepFunction() = IdentifiedLambda("keep", "Keep") { value: T -> Keep(value) }
private fun <FROM, TO> toDroppable(func: (SingleBuilder<FROM>) -> OptionalBuilder<TO>) =
  { from: SingleBuilder<FROM> -> func(from).map(keepFunction()).orElse(Drop) }

fun <FROM, TO> SingleBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> OptionalBuilder<TO>
): OptionalBuilder<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

fun <FROM, TO> OptionalBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> OptionalBuilder<TO>
): OptionalBuilder<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

fun <FROM, TO> ManyBuilder<FROM>.groupFilterWith(
  groupProducer: (SingleBuilder<FROM>) -> OptionalBuilder<TO>
): ManyBuilder<Pair<FROM, TO>> = groupFilterWith(toDroppable(groupProducer), dropPairFunction())

private fun <T> whereFunction() = IdentifiedLambda<(T, Boolean) -> Droppable<T>>("where", "Where") { value, keep ->
  if (keep) Keep(value) else Drop
}

fun <FROM> SingleBuilder<FROM>.where(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, Boolean>
): OptionalBuilder<FROM> = groupFilterWith(groupProducer, whereFunction())

fun <FROM> OptionalBuilder<FROM>.where(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, Boolean>
): OptionalBuilder<FROM> = groupFilterWith(groupProducer, whereFunction())

fun <FROM> ManyBuilder<FROM>.where(
  groupProducer: (SingleBuilder<FROM>) -> PipelineBuilder<*, Boolean>
): ManyBuilder<FROM> = groupFilterWith(groupProducer, whereFunction())
