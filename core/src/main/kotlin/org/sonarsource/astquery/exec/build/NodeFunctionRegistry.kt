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

import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import org.sonarsource.astquery.ir.nodes.IRNode
import kotlin.reflect.KClass

typealias NodeFunc = IdentifiedNodeFunction<*>
typealias RegistryKey = Pair<KClass<out IRNode<*, *>>, KClass<out NodeFunc>>

class NodeFunctionRegistry<CTX, N: Node<N>> {

    private val registry = mutableMapOf<RegistryKey, (CTX, IRNode<*, *>, NodeFunc) -> N>()

    fun <IR: IRNode<*, *>, F: NodeFunc> register(nodeType: KClass<IR>, funcType: KClass<out F>, translator: (CTX, IR, F) -> N) {
        @Suppress("UNCHECKED_CAST")
        registry[Pair(nodeType, funcType)] = translator as (CTX, IRNode<*, *>, NodeFunc) -> N
    }

    fun <IR: IRNode<*, *>> hasTranslator(nodeType: KClass<IR>, function: NodeFunc): Boolean {
        return registry.containsKey(Pair(nodeType, function::class))
    }

    fun translateNode(ctx: CTX, node: IRNode<*, *>, function: NodeFunc): N? {
        val translator = registry[Pair(node::class, function::class)]
        return translator?.invoke(ctx, node, function)
    }
}