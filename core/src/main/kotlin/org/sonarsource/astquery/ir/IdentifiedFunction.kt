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
package org.sonarsource.astquery.ir

import org.sonarsource.astquery.operation.Droppable
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

abstract class IdentifiedNodeFunction<out FUNC : Function<*>>(
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
