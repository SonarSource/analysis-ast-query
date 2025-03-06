package org.sonarsource.astquery.exec.greedy

import org.sonarsource.astquery.exec.greedy.specialized.CountNode
import org.sonarsource.astquery.exec.greedy.specialized.ExistsNode
import org.sonarsource.astquery.exec.greedy.specialized.first.FirstNode
import org.sonarsource.astquery.exec.greedy.specialized.first.FirstOrDefaultNode
import org.sonarsource.astquery.ir.FirstOrDefaultFunction
import org.sonarsource.astquery.ir.nodes.Aggregate

object NodeCreator {

    fun <IN, OUT> createFirstNode(ctx: GreedyBuildCtx, aggregate: Aggregate<IN, OUT>) =
        FirstNode(aggregate.id, ctx.getChildren(aggregate))

    @Suppress("UNCHECKED_CAST")
    fun <IN, OUT> createFirstOrDefaultNode(ctx: GreedyBuildCtx, aggregate: Aggregate<IN, OUT>, firstOrDefault: FirstOrDefaultFunction<*>) =
        FirstOrDefaultNode(aggregate.id, ctx.getChildren(aggregate), firstOrDefault.default as OUT)

    @Suppress("UNCHECKED_CAST")
    fun <IN> createCountNode(ctx: GreedyBuildCtx, aggregate: Aggregate<IN, *>) =
        CountNode<IN>(aggregate.id, ctx.getChildren(aggregate as Aggregate<IN, Int>))

    @Suppress("UNCHECKED_CAST")
    fun <IN> createExistsNode(ctx: GreedyBuildCtx, aggregate: Aggregate<IN, *>) =
        ExistsNode<IN>(aggregate.id, ctx.getChildren(aggregate as Aggregate<IN, Boolean>), false)

    @Suppress("UNCHECKED_CAST")
    fun <IN> createNotExistsNode(ctx: GreedyBuildCtx, aggregate: Aggregate<IN, *>) =
        ExistsNode<IN>(aggregate.id, ctx.getChildren(aggregate as Aggregate<IN, Boolean>), true)
}