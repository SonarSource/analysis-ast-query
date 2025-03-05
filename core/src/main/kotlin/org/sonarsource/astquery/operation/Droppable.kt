package org.sonarsource.astquery.operation

sealed class Droppable<out T> {
  data class Keep<T>(val data: T) : Droppable<T>()
  data object Drop : Droppable<Nothing>()
}