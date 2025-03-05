package org.sonarsource.astquery

import org.sonarsource.astquery.operation.builder.Selector
import org.sonarsource.astquery.operation.builder.SingleSelector
import org.sonarsource.astquery.exec.Executable
import org.sonarsource.astquery.ir.nodes.Root
import org.sonarsource.astquery.exec.ExecBuilder

class PipelineManager<INPUT>(
    private val builder: ExecBuilder<*>
) {
    private val root = Root<INPUT>()
    private val builderStart = SingleSelector(root)

    fun registerPipeline(operations: (SingleSelector<INPUT>) -> Unit) {
        operations(builderStart)
    }

    fun getExecutablePipeline(): Executable<INPUT> {
        return builder.build(root)
    }

    fun <INPUT, OUTPUT> createQuery(operations: (SingleSelector<INPUT>) -> Selector<*, OUTPUT>): Query<INPUT, OUTPUT> {
        return builder.createQuery(operations)
    }
}