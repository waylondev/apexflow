package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion

/**
 * Slow operation detector plugin for ApexFlow
 *
 * Automatically detects and reports operations that take longer than a specified threshold.
 * Helps identify performance bottlenecks without manual analysis.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPluginSlowOperationDetector(Duration.ofSeconds(1))
 * ```
 */
class ApexSlowOperationDetectorPlugin(
    private val threshold: Duration = Duration.ofSeconds(1),
    private val loggerName: String = "dev.waylon.apexflow.plugin.slow-operation"
) : ApexFlowPlugin {

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }

                // Measure the entire transform operation
                val startTime = Instant.now()

                return flow.transform(input)
                    .onCompletion { exception: Throwable? ->
                        val endTime = Instant.now()
                        val executionTime = Duration.between(startTime, endTime)

                        // Report slow operations
                        if (executionTime > threshold) {
                            logger.warn(
                                "Slow component execution detected: '$componentName' took $executionTime (threshold: $threshold)"
                            )
                        }
                    }
            }
        }
    }
}


