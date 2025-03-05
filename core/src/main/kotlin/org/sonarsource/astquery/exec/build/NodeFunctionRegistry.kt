package org.sonarsource.astquery.exec.build

import org.sonarsource.astquery.graph.Node
import org.sonarsource.astquery.ir.IdentifiedNodeFunction
import org.sonarsource.astquery.ir.nodes.IRNode
import kotlin.reflect.KClass

typealias RegistryKey = Pair<KClass<out IRNode<*, *>>, IdentifiedNodeFunction<*>>

class NodeFunctionRegistry<N: Node<N>> {

    private val registry = mutableMapOf<RegistryKey, (IRNode<*, *>) -> N>()

    inline fun <reified IR: IRNode<*, *>> register(function: IdentifiedNodeFunction<*>, noinline translator: (IR) -> N) {
        register(IR::class, function, translator)
    }

    fun <IR: IRNode<*, *>> register(nodeType: KClass<IR>, function: IdentifiedNodeFunction<*>, translator: (IR) -> N) {
        @Suppress("UNCHECKED_CAST")
        registry[Pair(nodeType, function)] = translator as (IRNode<*, *>) -> N
    }

    fun <IR: IRNode<*, *>> hasTranslator(nodeType: KClass<IR>, function: IdentifiedNodeFunction<*>): Boolean {
        return registry.containsKey(Pair(nodeType, function))
    }

    fun translateNode(node: IRNode<*, *>, function: IdentifiedNodeFunction<*>): N? {
        val translator = registry[Pair(node::class, function)]
        return translator?.invoke(node)
    }
}