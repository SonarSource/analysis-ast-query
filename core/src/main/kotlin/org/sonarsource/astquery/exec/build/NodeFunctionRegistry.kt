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