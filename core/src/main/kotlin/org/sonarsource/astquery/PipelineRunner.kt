package org.sonarsource.astquery

interface PipelineRunner<INPUT> {

    fun run(input: INPUT)
}