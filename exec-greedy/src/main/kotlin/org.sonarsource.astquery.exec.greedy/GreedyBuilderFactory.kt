package org.sonarsource.astquery.exec.greedy

import org.sonarsource.astquery.exec.build.BuilderFactory
import org.sonarsource.astquery.exec.build.NodeFunctionRegistry
import org.sonarsource.astquery.exec.greedy.NodeCreator.createCountNode
import org.sonarsource.astquery.exec.greedy.NodeCreator.createExistsNode
import org.sonarsource.astquery.exec.greedy.NodeCreator.createFirstNode
import org.sonarsource.astquery.exec.greedy.NodeCreator.createFirstOrDefaultNode
import org.sonarsource.astquery.exec.greedy.NodeCreator.createNotExistsNode
import org.sonarsource.astquery.exec.transformation.MergeEquivalentChildren
import org.sonarsource.astquery.exec.transformation.RemoveUnusedNodes
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.ir.*
import org.sonarsource.astquery.ir.nodes.Aggregate
import kotlin.reflect.KClass

class GreedyBuilderFactory : BuilderFactory<GreedyNode<*, *>, GreedyBuildCtx, GreedyBuilderFactory> {

    private val transformations = mutableListOf(
        RemoveUnusedNodes(),
        MergeEquivalentChildren()
    )

    private val nodeFuncRegistry = NodeFunctionRegistry<GreedyBuildCtx, GreedyNode<*, *>>()

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

    override fun addTransformation(transformation: Transformation<IR>): GreedyBuilderFactory {
        transformations.add(transformation)
        return this
    }

    inline fun <reified FROM : IR, reified F : IdentifiedNodeFunction<*>> registerSpecializedNode(
        noinline provider: (GreedyBuildCtx, FROM, F) -> GreedyNode<*, *>
    ): GreedyBuilderFactory {
        return registerSpecializedNode(FROM::class, F::class, provider)
    }

    override fun <FROM : IR, F : IdentifiedNodeFunction<*>> registerSpecializedNode(
        irType: KClass<out FROM>,
        function: KClass<out F>,
        provider: (GreedyBuildCtx, FROM, F) -> GreedyNode<*, *>
    ): GreedyBuilderFactory {
        nodeFuncRegistry.register(irType, function, provider)
        return this
    }

    override fun build(): GreedyBuilder {
        return GreedyBuilder(transformations, nodeFuncRegistry)
    }
}