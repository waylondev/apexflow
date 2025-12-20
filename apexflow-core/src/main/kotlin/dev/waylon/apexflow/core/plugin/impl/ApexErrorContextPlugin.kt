package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * Error context plugin for ApexFlow
 *
 * Provides detailed error context, including the component where the error occurred,
 * input data summary, and execution stack. Helps quickly locate and fix errors.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val errorHandledFlow = flow.withPluginErrorContext()
 * ```
 */
class ApexErrorContextPlugin(private val loggerName: String = "dev.waylon.apexflow.plugin.error-context") : ApexFlowPlugin {

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)
        val executionId = AtomicLong(System.currentTimeMillis())

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }
                val flowId = "flow-${executionId.incrementAndGet()}"
                var lastProcessedInput: Any? = null

                return input
                    .onEach { inputItem ->
                        // Store the last processed input for error context
                        lastProcessedInput = inputItem
                    }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .catch { e ->
                        val errorContext = buildString {
                            appendLine("Error Context:")
                            appendLine("- Flow ID: $flowId")
                            appendLine("- Component: $componentName")
                            appendLine("- Timestamp: ${Instant.now()}")
                            appendLine("- Last Processed Input: $lastProcessedInput")
                            appendLine("- Error Message: ${e.message}")
                        }
                        
                        logger.error(errorContext, e)
                        throw e // Re-throw to preserve original error handling
                    }
            }
        }
    }
}
