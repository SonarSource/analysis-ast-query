package org.sonarsource.astquery.exec.build

import org.sonarsource.astquery.PipelineManager
import org.sonarsource.astquery.exec.transformation.Transformation
import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.ir.IR
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import kotlin.reflect.KClass

interface BuilderFactory<N : Node<N>, CTX, B : BuilderFactory<N, CTX, B>> {

    fun addTransformation(transformation: Transformation<IR>): B

    fun <FROM : IR, F : IdentifiedNodeFunction<*>> registerSpecializedNode(
        irType: KClass<out FROM>, function: KClass<out F>, provider: (CTX, FROM, F) -> N
    ): B

    fun <IN> createManager(): PipelineManager<IN>
}