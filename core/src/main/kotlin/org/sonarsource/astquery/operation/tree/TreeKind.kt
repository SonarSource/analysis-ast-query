/*
 * SonarQube Java
 * Copyright (C) 2012-2024 SonarSource SA
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

package org.sonar.plugins.java.api.query.operation.tree

import org.sonar.plugins.java.api.query.ManySelector
import org.sonar.plugins.java.api.query.OptionalSelector
import org.sonar.plugins.java.api.query.SingleSelector
import org.sonar.plugins.java.api.query.operation.core.filter
import org.sonar.plugins.java.api.query.tree.TreeKind
import org.sonar.plugins.java.api.tree.Tree

@Suppress("UNCHECKED_CAST")
fun <I : Tree, O : Tree> SingleSelector<I>.ofKind(vararg kinds: TreeKind<out O>): OptionalSelector<O> =
  filter { tree -> kinds.any { it.kind == tree.kind() } } as OptionalSelector<O>

@Suppress("UNCHECKED_CAST")
fun <I : Tree, O : Tree> OptionalSelector<I>.ofKind(vararg kinds: TreeKind<out O>): OptionalSelector<O> =
  filter { tree -> kinds.any { it.kind == tree.kind() } } as OptionalSelector<O>

@Suppress("UNCHECKED_CAST")
fun <I : Tree, O : Tree> ManySelector<I>.ofKind(vararg kinds: TreeKind<out O>): ManySelector<O> =
  filter { tree -> kinds.any { it.kind == tree.kind() } } as ManySelector<O>
