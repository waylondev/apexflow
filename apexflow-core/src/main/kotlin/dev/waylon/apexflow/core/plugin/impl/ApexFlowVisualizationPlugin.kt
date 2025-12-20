package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * Flow visualization plugin for ApexFlow
 *
 * Generates flow diagrams or execution trace graphs, intuitively displaying data flow between components.
 * Helps understand complex flows through visualization.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val visualizedFlow = flow.withPluginFlowVisualization()
 * ```
 */
class ApexFlowVisualizationPlugin(
    private val loggerName: String = "dev.waylon.apexflow.plugin.flow-visualization",
    private val outputFormat: VisualizationFormat = VisualizationFormat.TEXT
) : ApexFlowPlugin {

    /**
     * Visualization output formats
     */
    enum class VisualizationFormat {
        /** Simple text-based visualization */
        TEXT,

        /** DOT format for graphviz visualization */
        DOT
    }

    private val executionTrace = ConcurrentLinkedQueue<ExecutionEvent>()
    private val executionId = System.currentTimeMillis()

    /**
     * Execution event data class
     */
    private data class ExecutionEvent(
        val componentName: String,
        val eventType: String,
        val timestamp: Instant,
        val metadata: Map<String, Any> = emptyMap()
    )

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }

                return input
                    .onStart {
                        executionTrace.add(
                            ExecutionEvent(
                                componentName = componentName,
                                eventType = "START",
                                timestamp = Instant.now(),
                                metadata = mapOf("flowId" to executionId)
                            )
                        )
                    }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onCompletion { exception: Throwable? ->
                        executionTrace.add(
                            ExecutionEvent(
                                componentName = componentName,
                                eventType = if (exception == null) "COMPLETE" else "ERROR",
                                timestamp = Instant.now(),
                                metadata = mapOf(
                                    "flowId" to executionId,
                                    "exception" to (exception?.message ?: "none")
                                )
                            )
                        )

                        // Generate and log visualization when flow completes
                        val visualization = generateVisualization()
                        logger.info("Flow Execution Visualization (ID: $executionId):\n$visualization")
                    }
            }
        }
    }

    /**
     * Generate visualization based on execution trace
     */
    private fun generateVisualization(): String {
        return when (outputFormat) {
            VisualizationFormat.TEXT -> generateTextVisualization()
            VisualizationFormat.DOT -> generateDotVisualization()
        }
    }

    /**
     * Generate text-based visualization
     */
    private fun generateTextVisualization(): String {
        val sb = StringBuilder()
        sb.appendLine("# ApexFlow Execution Trace")
        sb.appendLine("ID: $executionId")
        sb.appendLine("Timestamp: ${Instant.now()}")
        sb.appendLine("Events:")

        executionTrace.forEachIndexed { index, event ->
            sb.appendLine("${index + 1}. [${event.timestamp}] ${event.eventType} - ${event.componentName}")
            if (event.metadata.isNotEmpty()) {
                event.metadata.forEach { (key, value) ->
                    sb.appendLine("   $key: $value")
                }
            }
        }

        return sb.toString()
    }

    /**
     * Generate DOT format visualization for use with graphviz
     */
    private fun generateDotVisualization(): String {
        val sb = StringBuilder()
        sb.appendLine("digraph ApexFlowExecution {")
        sb.appendLine("    rankdir=LR;")
        sb.appendLine("    node [shape=box, style=rounded];")
        sb.appendLine("    graph [label=\"ApexFlow Execution Trace ID: $executionId\", labelloc=top];")

        // Add components as nodes
        val components = executionTrace.map { it.componentName }.toSet()
        components.forEach { component ->
            sb.appendLine("    \"$component\" [style=filled, fillcolor=lightblue];")
        }

        // Add execution flow as edges
        executionTrace.windowed(2, step = 1, partialWindows = false).forEachIndexed { index, events ->
            val (first, second) = events
            sb.appendLine("    \"${first.componentName}\" -> \"${second.componentName}\" [label=\"Step ${index + 1}\"];")
        }

        sb.appendLine("}")
        return sb.toString()
    }
}
