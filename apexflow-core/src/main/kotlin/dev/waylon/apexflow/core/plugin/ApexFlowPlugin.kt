package dev.waylon.apexflow.core.plugin

import dev.waylon.apexflow.core.ApexFlow

/**
 * Plugin interface for ApexFlow, used to extend ApexFlow functionality through the Decorator Pattern
 * 
 * This interface enables powerful extension capabilities while maintaining the "Everything is Flow" principle.
 * Plugins can add additional features like logging, monitoring, retry logic, caching, and more.
 * 
 * **Design Principles:**
 * - **Open/Closed Principle**: ApexFlow can be extended without modifying core code
 * - **Decorator Pattern**: Plugins wrap existing flows, allowing composition
 * - **Type Safety**: Full generic support maintains type safety
 * - **No Runtime Overhead**: Compile-time resolution of plugin chains
 * 
 * **Core Features:**
 * - **Universal Compatibility**: Works with any ApexFlow instance
 * - **Plugin Composition**: Multiple plugins can be applied in sequence
 * - **Bidirectional Transformation**: Can modify both input and output flows
 * - **Context Preservation**: Maintains flow context throughout transformations
 * 
 * **Usage Example - Creating a Custom Plugin:**
 * ```kotlin
 * class TimingPlugin : ApexFlowPlugin {
 *     override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
 *         return object : ApexFlow<I, O> {
 *             override fun transform(input: Flow<I>): Flow<O> {
 *                 return input
 *                     .onEach { println("Processing started at: ${System.currentTimeMillis()}") }
 *                     .let { flow.transform(it) }
 *                     .onEach { println("Processing completed at: ${System.currentTimeMillis()}") }
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * **Usage Example - Applying Plugins:**
 * ```kotlin
 * val workflow = apexFlow<Int, String> { 
 *     map { "Processed: $it" }
 * }
 * 
 * // Apply single plugin
 * val timedWorkflow = workflow.withPlugin(TimingPlugin())
 * 
 * // Apply multiple plugins in sequence
 * val enhancedWorkflow = workflow
 *     .withPlugin(TimingPlugin())
 *     .withPlugin(LoggingPlugin())
 *     .withPlugin(CachingPlugin())
 * ```
 * 
 * **Core Plugin Types:**
 * - **Logging**: Track flow execution and performance
 * - **Monitoring**: Collect metrics and health data
 * - **Retry**: Automatic retry logic for failed operations
 * - **Caching**: Cache results for improved performance
 * - **Validation**: Input/output validation
 * - **Security**: Authentication and authorization
 * 
 * @see dev.waylon.apexflow.core.plugin.impl.LoggingPlugin for a reference implementation
 * @see dev.waylon.apexflow.core.dsl.withPlugin for plugin application
 */
interface ApexFlowPlugin {
    /**
     * Wrap the original flow, adding plugin functionality
     * 
     * This method is the core of the plugin system. Implementations should:
     * 1. Receive the original flow
     * 2. Create a new flow that adds plugin functionality
     * 3. Call the original flow's transform method appropriately
     * 4. Return the enhanced flow
     * 
     * @param flow The original flow to wrap and enhance
     * @return A new ApexFlow instance with plugin functionality added
     */
    fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O>
}
