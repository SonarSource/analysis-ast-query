package org.sonarsource.astquery.exec.batch

import org.sonarsource.astquery.exec.build.BuilderFactory
import org.sonarsource.astquery.exec.build.NodeFunctionRegistry
import org.sonarsource.astquery.exec.batch.NodeCreator.createCountNode
import org.sonarsource.astquery.exec.batch.NodeCreator.createExistsNode
import org.sonarsource.astquery.exec.batch.NodeCreator.createFirstNode
import org.sonarsource.astquery.exec.batch.NodeCreator.createFirstOrDefaultNode
import org.sonarsource.astquery.exec.batch.NodeCreator.createNotExistsNode
import org.sonarsource.astquery.exec.transformation.MergeEquivalentChildren
import org.sonarsource.astquery.exec.transformation.RemoveUnusedNodes
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.ir.CountFunction
import org.sonarsource.astquery.ir.ExistFunction
import org.sonarsource.astquery.ir.FirstFunction
import org.sonarsource.astquery.ir.FirstOrDefaultFunction
import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import org.sonarsource.astquery.ir.NotExistFunction
import org.sonarsource.astquery.ir.nodes.Aggregate
import kotlin.reflect.KClass

class BatchBuilderFactory : BuilderFactory<BatchNode<*, *>, BatchBuildCtx, BatchBuilderFactory> {

    private val transformations = mutableListOf(
        RemoveUnusedNodes(),
        MergeEquivalentChildren()
    )

    private val nodeFuncRegistry = NodeFunctionRegistry<BatchBuildCtx, BatchNode<*, *>>()

    init {
        registerSpecializedNode<Aggregate<*, *>, FirstFunction<*>> { ctx, agg, _ ->
            createFirstNode(ctx, agg)
        }
        registerSpecializedNode<Aggregate<*, *>, FirstOrDefaultFunction<*>> { ctx, agg, firstOrDefault ->
            createFirstOrDefaultNode(ctx, agg, firstOrDefault)
        }
        registerSpecializedNode<Aggregate<*, *>, CountFunction> { ctx, agg, _ ->
            createCountNode(ctx, agg)
        }
        registerSpecializedNode<Aggregate<*, *>, ExistFunction> { ctx, agg, _ ->
            createExistsNode(ctx, agg)
        }
        registerSpecializedNode<Aggregate<*, *>, NotExistFunction> { ctx, agg, _ ->
            createNotExistsNode(ctx, agg)
        }
    }

    override fun addTransformation(transformation: Transformation<IR>): BatchBuilderFactory {
        transformations.add(transformation)
        return this
    }

    inline fun <reified FROM : IR, reified F : IdentifiedNodeFunction<*>> registerSpecializedNode(
        noinline provider: (BatchBuildCtx, FROM, F) -> BatchNode<*, *>
    ): BatchBuilderFactory {
        return registerSpecializedNode(FROM::class, F::class, provider)
    }

    override fun <FROM : IR, F : IdentifiedNodeFunction<*>> registerSpecializedNode(
        irType: KClass<out FROM>,
        function: KClass<out F>,
        provider: (BatchBuildCtx, FROM, F) -> BatchNode<*, *>
    ): BatchBuilderFactory {
        nodeFuncRegistry.register(irType, function, provider)
        return this
    }

    override fun build(): BatchBuilder {
        return BatchBuilder(transformations, nodeFuncRegistry)
    }
}