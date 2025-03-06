package org.sonarsource.astquery.exec

class MetadataGenerator<INPUT> {

    val entries = mutableMapOf<ContextEntry<*>, (INPUT) -> Any?>()

    fun <T> addMetadata(entry: ContextEntry<T>, provider: (INPUT) -> T) {
        entries[entry] = provider
    }

    fun generateMetadata(input: INPUT): Map<ContextEntry<*>, *> {
        return entries.mapValues { it.value(input) }
    }
}
