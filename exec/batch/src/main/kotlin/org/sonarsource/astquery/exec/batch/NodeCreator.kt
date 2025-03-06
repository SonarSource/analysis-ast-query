package org.sonarsource.astquery.exec.batch

import org.sonarsource.astquery.exec.batch.specialized.CountNode
import org.sonarsource.astquery.exec.batch.specialized.ExistsNode
import org.sonarsource.astquery.exec.batch.specialized.first.FirstNode
import org.sonarsource.astquery.exec.batch.specialized.first.FirstOrDefaultNode
import org.sonarsource.astquery.ir.FirstOrDefaultFunction
import org.sonarsource.astquery.ir.nodes.Aggregate

object NodeCreator {

    fun <IN, OUT> createFirstNode(ctx: BatchBuildCtx, aggregate: Aggregate<IN, OUT>) =
        FirstNode(aggregate.id, ctx.getChildren(aggregate))

    @Suppress("UNCHECKED_CAST")
    fun <IN, OUT> createFirstOrDefaultNode(ctx: BatchBuildCtx, aggregate: Aggregate<IN, OUT>, firstOrDefault: FirstOrDefaultFunction<*>) =
        FirstOrDefaultNode(aggregate.id, ctx.getChildren(aggregate), firstOrDefault.default as OUT)

    @Suppress("UNCHECKED_CAST")
    fun <IN> createCountNode(ctx: BatchBuildCtx, aggregate: Aggregate<IN, *>) =
        CountNode<IN>(aggregate.id, ctx.getChildren(aggregate as Aggregate<IN, Int>))

    @Suppress("UNCHECKED_CAST")
    fun <IN> createExistsNode(ctx: BatchBuildCtx, aggregate: Aggregate<IN, *>) =
        ExistsNode<IN>(aggregate.id, ctx.getChildren(aggregate as Aggregate<IN, Boolean>), false)

    @Suppress("UNCHECKED_CAST")
    fun <IN> createNotExistsNode(ctx: BatchBuildCtx, aggregate: Aggregate<IN, *>) =
        ExistsNode<IN>(aggregate.id, ctx.getChildren(aggregate as Aggregate<IN, Boolean>), true)
}