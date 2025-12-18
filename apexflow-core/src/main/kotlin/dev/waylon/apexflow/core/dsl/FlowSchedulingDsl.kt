package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Simple transformation operation with explicit coroutine dispatcher
 * 
 * This function combines [flowOn()] and [map()] into a single convenient operation,
 * allowing explicit dispatcher specification for transformations. This follows the Single Responsibility Principle
 * by separating transformation logic from dispatcher management.
 * 
 * **Key Benefits:**
 * - **Clear Dispatcher Specification**: Explicitly define which dispatcher to use
 * - **Reduced Boilerplate**: Combines two operations into one
 * - **Improved Readability**: Clear intent about execution context
 * - **Type Safety**: Comprehensive compile-time checks
 * 
 * **Usage Examples:**
 * ```kotlin
 * // Basic usage with explicit dispatcher
 * flow.transformOn(Dispatchers.IO) {
 *     // IO-intensive operation
 *     file.readText()
 * }
 * 
 * // Used within apexFlow DSL
 * val myFlow = apexFlow<Int, String> {
 *     transformOn(Dispatchers.Default) {
 *         // CPU-intensive calculation
 *         complexCalculation(it)
 *     }
 * }
 * ```
 * 
 * @param dispatcher CoroutineDispatcher to run the transformation on
 * @param block Transformation function that runs on the specified dispatcher
 * @return Flow with transformation applied, running on the specified dispatcher
 */
@ApexFlowDsl
inline fun <I, O> Flow<I>.transformOn(
    dispatcher: CoroutineDispatcher,
    crossinline block: suspend (I) -> O
): Flow<O> {
    return this.flowOn(dispatcher).map(block)
}

/**
 * Extension function: IO-intensive transformation operation
 * 
 * Convenience function for IO-bound operations (file I/O, network calls, database queries).
 * Runs on [Dispatchers.IO] dispatcher, which is optimized for blocking I/O operations.
 * 
 * **Common Use Cases:**
 * - File system operations (reading/writing files)
 * - Network requests (HTTP calls, API requests)
 * - Database queries (SQL operations, ORM interactions)
 * - External service calls (gRPC, messaging systems)
 * 
 * **Usage Examples:**
 * ```kotlin
 * // Basic usage for database operations
 * flow.transformOnIO {
 *     database.findById(it)
 * }
 * 
 * // Used within apexFlow DSL for file processing
 * val fileFlow = apexFlow<String, String> {
 *     transformOnIO { filePath ->
 *         File(filePath).readText()
 *     }
 * }
 * 
 * // Chaining multiple IO operations
 * val multiStepFlow = apexFlow<Int, String> {
 *     transformOnIO { id ->
 *         database.getUser(id)
 *     }
 *     .transformOnIO { user ->
 *         externalService.enrichUser(user)
 *     }
 *     .transformOnIO { user ->
 *         cache.storeUser(user)
 *     }
 *     .map { user ->
 *         user.toDto()
 *     }
 * }
 * ```
 * 
 * @param block IO-intensive transformation function
 * @return Flow with transformation applied on Dispatchers.IO
 */
@ApexFlowDsl
inline fun <I, O> Flow<I>.transformOnIO(crossinline block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.IO, block)
}

/**
 * Extension function: CPU-intensive transformation operation
 * 
 * Convenience function for CPU-bound operations (calculations, computations).
 * Runs on [Dispatchers.Default] dispatcher, which is optimized for CPU-intensive tasks.
 * 
 * **Common Use Cases:**
 * - Mathematical calculations (complex formulas, statistics)
 * - Data processing (parsing, serialization, encryption)
 * - Algorithm execution (sorting, searching, machine learning)
 * - Image/audio processing (compression, filtering)
 * 
 * **Usage Examples:**
 * ```kotlin
 * // Basic usage for calculations
 * flow.transformOnDefault {
 *     complexCalculation(it)
 * }
 * 
 * // Used within apexFlow DSL for data processing
 * val processingFlow = apexFlow<List<Data>, List<Result>> {
 *     transformOnDefault {
 *         it.parallelStream()
 *             .map { data -> processData(data) }
 *             .toList()
 *     }
 * }
 * 
 * // Chaining CPU and IO operations appropriately
 * val hybridFlow = apexFlow<String, Result> {
 *     transformOnIO { filePath ->
 *         File(filePath).readBytes()
 *     }
 *     .transformOnDefault { bytes ->
 *         heavyComputation(bytes)
 *     }
 *     .transformOnIO { result ->
 *         database.saveResult(result)
 *     }
 * }
 * ```
 * 
 * @param block CPU-intensive transformation function
 * @return Flow with transformation applied on Dispatchers.Default
 */
@ApexFlowDsl
inline fun <I, O> Flow<I>.transformOnDefault(crossinline block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Default, block)
}
