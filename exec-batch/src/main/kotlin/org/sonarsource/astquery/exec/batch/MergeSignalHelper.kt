package org.sonarsource.astquery.exec.batch

object MergeSignalHelper {

    fun <A, B> processScope(
        queueA: List<Signal<A>>,
        queueB: List<Signal<B>>,
        scopeEnd: Signal.ScopeEnd,
        processValues: (Signal.Value<A>, Signal.Value<B>, Boolean) -> Unit,
        processSignal: (Signal<Nothing>) -> Unit,
    ): Pair<List<Signal<A>>, List<Signal<B>>>? {
        val upToA = queueA.indexOf(scopeEnd)
        val upToB = queueB.indexOf(scopeEnd)

        if (upToA == -1 || upToB == -1) {
            return null
        }

        val scopedA = queueA.take(upToA)
        val scopedB = queueB.take(upToB)

        processSignals(scopedA, scopedB, processValues, processSignal)
      
        processSignal(scopeEnd)

        val nextA = queueA.drop(upToA + 1)
        val nextB = queueB.drop(upToB + 1)

        return nextA to nextB
    }

    private fun <A, B> processSignals(
        signalsA: List<Signal<A>>,
        signalsB: List<Signal<B>>,
        processValues: (Signal.Value<A>, Signal.Value<B>, Boolean) -> Unit,
        processSignal: (Signal<Nothing>) -> Unit,
    ) {
        // For now, this is done is a product way, the reason is that it will handle every possibility.
        // But in most cases, one list will have one signal

        var firstMerge = true

        for (signalA in signalsA) {
            when (signalA) {
                is Signal.ScopeEnd -> processSignal(signalA)

                is Signal.Value<A> -> {
                    for (signalB in signalsB) {
                        when (signalB) {
                            is Signal.ScopeEnd -> processSignal(signalB)

                            is Signal.Value<B> -> {
                                if (signalA.scopes.isSameAs(signalB.scopes)) {
                                    processValues(signalA, signalB, firstMerge)
                                    firstMerge = false
                                }
                            }
                        }
                    }
                }

            }
            firstMerge = true
        }
    }
}
