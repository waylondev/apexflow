package dev.waylon.apexflow.core.types

import kotlinx.coroutines.flow.Flow

/**
 * Modern type definitions for ApexFlow
 * Leveraging Kotlin 2.3.0's improved type system
 */

/**
 * Sealed class representing different types of flow transformations
 * Provides exhaustive type checking in Kotlin 2.3.0
 */
public sealed class FlowTransformation<I, O> {
    /** Identity transformation - passes input unchanged */
    public data object Identity : FlowTransformation<Any, Any>()
    
    /** Mapping transformation - converts input to output */
    public data class Map<I, O>(val transform: suspend (I) -> O) : FlowTransformation<I, O>()
    
    /** Filtering transformation - includes only items that match condition */
    public data class Filter<I>(val predicate: suspend (I) -> Boolean) : FlowTransformation<I, I>()
    
    /** Flat mapping transformation - expands each item to multiple items */
    public data class FlatMap<I, O>(val transform: suspend (I) -> Flow<O>) : FlowTransformation<I, O>()
    
    /** Parallel transformation - processes items in parallel */
    public data class Parallel<I, O>(val transform: suspend (I) -> O, val concurrency: Int = 4) : FlowTransformation<I, O>()
    
    /** Conditional transformation - applies transformation only when condition is met */
    public data class Conditional<I, O>(val condition: suspend (I) -> Boolean, val transform: suspend (I) -> O) : FlowTransformation<I, O>()
}

/**
 * Modern type aliases for Flow operations
 * Using Kotlin 2.3.0's improved type inference
 */
public typealias FlowMapper<I, O> = suspend (I) -> O
public typealias FlowPredicate<I> = suspend (I) -> Boolean
public typealias FlowFlatMapper<I, O> = suspend (I) -> Flow<O>
public typealias FlowTransformer<I, O> = Flow<I>.() -> Flow<O>

/**
 * Flow result wrapper with modern sealed class design
 */
public sealed class FlowResult<out T> {
    /** Success result with value */
    public data class Success<T>(val value: T) : FlowResult<T>()
    
    /** Error result with exception */
    public data class Error(val exception: Throwable) : FlowResult<Nothing>()
    
    /** Empty result */
    public data object Empty : FlowResult<Nothing>()
    
    /** Cancelled result */
    public data object Cancelled : FlowResult<Nothing>()
}

/**
 * Convenience extension functions for FlowResult
 */
public fun <T> FlowResult<T>.isSuccess(): Boolean = this is FlowResult.Success
public fun <T> FlowResult<T>.isError(): Boolean = this is FlowResult.Error
public fun <T> FlowResult<T>.valueOrNull(): T? = (this as? FlowResult.Success)?.value
public fun <T> FlowResult<T>.exceptionOrNull(): Throwable? = (this as? FlowResult.Error)?.exception

/**
 * Flow execution metadata
 * Using Kotlin 2.3.0's modern data class features
 */
public data class FlowMetadata(
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val itemsProcessed: Int = 0,
    val itemsSkipped: Int = 0,
    val errors: Int = 0,
    val concurrencyLevel: Int = 4,
) {
    /** Duration of flow execution in milliseconds */
    public val duration: Long? get() = endTime?.minus(startTime)
}
