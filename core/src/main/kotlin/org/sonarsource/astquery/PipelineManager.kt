package org.sonarsource.astquery

import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder
import org.sonarsource.astquery.exec.Executable
import org.sonarsource.astquery.ir.nodes.Root
import org.sonarsource.astquery.exec.build.ExecBuilder

class PipelineManager<INPUT>(
    private val builder: ExecBuilder
) {
    private val root = Root<INPUT>()
    private val builderStart = SingleBuilder(root)

    fun registerPipeline(operations: (SingleBuilder<INPUT>) -> Unit) {
        operations(builderStart)
    }

    fun getExecutablePipeline(): Executable<INPUT> {
        return builder.build(root)
    }

    fun <INPUT, OUTPUT> createQuery(operations: (SingleBuilder<INPUT>) -> PipelineBuilder<*, OUTPUT>): Query<INPUT, OUTPUT> {
        return builder.createQuery(operations)
    }
}