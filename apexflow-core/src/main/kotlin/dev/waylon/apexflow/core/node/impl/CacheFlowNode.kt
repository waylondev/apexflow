package dev.waylon.apexflow.core.node.impl

import dev.waylon.apexflow.core.node.FlowNode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
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
    // We'll track cache membership separately for null values
    private val cache = ConcurrentHashMap<I, O>()
    private val cacheContainsNull = ConcurrentHashMap<I, AtomicBoolean>()

    override fun transform(input: Flow<I>): Flow<O> {
        return input.map { inputData ->
            // Check if we've cached a null result
            if (cacheContainsNull.containsKey(inputData)) {
                null as O
            } else {
                // Check cache for non-null result
                cache[inputData] ?: run {
                    // Cache miss, execute transformation and cache result
                    val result = transformer(inputData)
                    if (result == null) {
                        // Track null result separately
                        cacheContainsNull[inputData] = AtomicBoolean(true)
                    } else {
                        cache[inputData] = result
                    }
                    result
                }
            }
        }
    }

    /**
     * Clear the cache
     */
    fun clearCache() {
        cache.clear()
        cacheContainsNull.clear()
    }

    /**
     * Get the current cache size
     */
    fun cacheSize(): Int {
        return cache.size + cacheContainsNull.size
    }
}
