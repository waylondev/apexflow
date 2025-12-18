package dev.waylon.apexflow.core.node.impl

import dev.waylon.apexflow.core.node.FlowNode
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Cache node for caching transformation results
 * Typical FlowNode implementation demonstrating state management and caching functionality
 * @param I Input type
 * @param O Output type
 * @property transformer Transformation function
 */
class CacheFlowNode<I, O>(
    private val transformer: suspend (I) -> O
) : FlowNode<I, O> {

    // Using ConcurrentHashMap as thread-safe cache
    private val cache = ConcurrentHashMap<I, O>()

    override fun transform(input: Flow<I>): Flow<O> {
        return input.map { inputData ->
            // Check cache, return directly if exists
            cache[inputData] ?: run {
                // Cache miss, execute transformation and cache result
                val result = transformer(inputData)
                cache[inputData] = result
                result
            }
        }
    }

    /**
     * Clear the cache
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * Get the current cache size
     */
    fun cacheSize(): Int {
        return cache.size
    }
}
