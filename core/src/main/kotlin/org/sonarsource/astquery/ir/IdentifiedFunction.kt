package org.sonar.plugins.java.api.query.graph.ir

import org.sonarsource.astquery.operation.Droppable
import org.sonar.plugins.java.api.tree.Tree
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

typealias OpId = String

sealed interface IdentifiedFunction<out FUNC : Function<*>> {
  val id: OpId?
  val name: String

  companion object {
    fun <IN : Any, OUT> fromMember(clazz: KClass<IN>, prop: KProperty1<IN, OUT>) =
      IdentifiedLambda(
        id = "property.${clazz.qualifiedName}.${prop.name}",
        desc = prop.name,
        function = prop::get
      )

    fun <IN : Any, OUT> fromMember(clazz: KClass<IN>, func: KFunction1<IN, OUT>) =
      IdentifiedLambda(
        id = "function.${clazz.qualifiedName}.${func.name}()",
        desc = "${func.name}()",
        function = func::invoke
      )
  }
}

data class IdentifiedLambda<out FUNC : Function<*>>(
  override val id: OpId?,
  val desc: String?,
  val function: FUNC
) : IdentifiedFunction<FUNC> {

  override val name = desc ?: "Lambda"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    id ?: return false

    if (javaClass != other?.javaClass) return false
    other as IdentifiedLambda<*>

    return id == other.id
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: super.hashCode()
  }
}

fun <T> identity(name: String = "Identity") = IdentifiedLambda("identity", name) { input: T -> input }
fun <T> constant(value: T, name: String = "Constant-$value") =
  IdentifiedLambda("constant.$value", name) { _: Any? -> value }

sealed class IdentifiedNodeFunction<out FUNC : Function<*>>(
  final override val name: String
) : IdentifiedFunction<FUNC> {
  final override val id: OpId = "NodeOperation[$name]"

  override fun toString() = name
}

data object ExistFunction : IdentifiedNodeFunction<(List<*>) -> Boolean>("Exists")
data object NotExistFunction : IdentifiedNodeFunction<(List<*>) -> Boolean>("NotExists")
class FirstFunction<T> : IdentifiedNodeFunction<(List<T>) -> Droppable<T>>("First") {
  override fun equals(other: Any?): Boolean {
    return other is FirstFunction<*>
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }
}
data class FirstOrDefaultFunction<T>(val default: T) : IdentifiedNodeFunction<(List<T>) -> T>("FirstOrDefault($default)")
data object CountFunction : IdentifiedNodeFunction<(List<*>) -> Int>("Count")

data class TreeIsOfKindFunction(val kinds: Set<Tree.Kind>) : IdentifiedNodeFunction<(Tree) -> Boolean>("Tree.IsOfKind")
data object TreeParentFunction : IdentifiedNodeFunction<(Tree) -> Sequence<Tree>>("Tree.Parent")
data class SubtreeFunction(
  val includeRoot: Boolean = false,
  val stopAt: Set<Tree.Kind>
) : IdentifiedNodeFunction<(Tree) -> Sequence<Tree>>(
  if (includeRoot) "Tree" else "SubTree" + stopAt.joinToString(", ", "(", ")")
)
