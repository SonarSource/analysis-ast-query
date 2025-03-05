package org.sonarsource.astquery.ir

import org.sonarsource.astquery.graph.Graph
import org.sonarsource.astquery.ir.nodes.IRNode
import org.sonarsource.astquery.ir.nodes.Root

typealias IR = IRNode<*, *>
typealias IRGraph<IN> = Graph<Root<IN>, IR>
