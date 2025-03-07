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
package org.sonarsource.astquery.exec.build

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

    fun build(): ExecBuilder
}