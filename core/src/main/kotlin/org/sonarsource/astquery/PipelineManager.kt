/*
 * Copyright (C) 2018-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.astquery

import org.sonarsource.astquery.exec.ContextEntry
import org.sonarsource.astquery.exec.ExecutionContext
import org.sonarsource.astquery.exec.MetadataGenerator
import org.sonarsource.astquery.exec.build.BuilderFactory
import org.sonarsource.astquery.exec.build.ExecBuilder
import org.sonarsource.astquery.ir.nodes.Root
import org.sonarsource.astquery.operation.builder.PipelineBuilder
import org.sonarsource.astquery.operation.builder.SingleBuilder

class PipelineManager<INPUT>(
    private val builder: ExecBuilder,
    private val metadataGenerator: MetadataGenerator<INPUT>,
) {
    private val root = Root<INPUT>()
    private val builderStart = SingleBuilder(root)

    fun registerPipeline(operations: (SingleBuilder<INPUT>) -> Unit) {
        operations(builderStart)
    }

    fun getExecutablePipeline(): PipelineRunner<INPUT>? {
        val exec = builder.build(root)
        if (exec.isEmpty()) {
            return null
        }

        return object : PipelineRunner<INPUT> {
            override fun run(input: INPUT) {
                val metadata = metadataGenerator.generateMetadata(input)
                val context = ExecutionContext(metadata)
                exec.execute(context, input)
            }
        }
    }

    fun <INPUT, OUTPUT> createQuery(operations: (SingleBuilder<INPUT>) -> PipelineBuilder<*, OUTPUT>): Query<INPUT, OUTPUT> {
        return builder.createQuery(operations)
    }

    class Builder<INPUT> {
        private var execBuilder: ExecBuilder? = null
        private var metadataGenerator = MetadataGenerator<INPUT>()

        fun withExecBuilder(factory: BuilderFactory<*, *, *>): Builder<INPUT> {
            this.execBuilder = factory.build()
            return this
        }

        fun <T> addMetadata(entry: ContextEntry<T>, provider: (INPUT) -> T): Builder<INPUT> {
            metadataGenerator.addMetadata(entry, provider)
            return this
        }

        fun build(): PipelineManager<INPUT> {
            return PipelineManager(
                execBuilder ?: throw IllegalStateException("ExecBuilder is required"),
                metadataGenerator
            )
        }
    }
}