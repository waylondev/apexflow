package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * Throughput plugin for ApexFlow
 *
 * Monitors the number of items processed per unit time, calculating average and peak throughput.
 * Helps understand flow processing capacity and evaluate system performance under high load.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPluginThroughput()
 * ```
 */
class ApexThroughputPlugin(
    private val loggerName: String = "dev.waylon.apexflow.plugin.throughput",
    private val samplingInterval: Duration = Duration.ofSeconds(1)
) : ApexFlowPlugin {

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }
                val startTime = Instant.now()
                val itemCount = AtomicLong(0)
                val lastSampleTime = AtomicLong(System.currentTimeMillis())
                val lastItemCount = AtomicLong(0)

                return input
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onEach { _ ->
                        val currentCount = itemCount.incrementAndGet()
                        val now = System.currentTimeMillis()
                        val lastSample = lastSampleTime.get()

                        // Calculate throughput every sampling interval
                        if (now - lastSample >= samplingInterval.toMillis()) {
                            val countSinceLastSample = currentCount - lastItemCount.get()
                            val throughput = countSinceLastSample.toDouble() / samplingInterval.seconds

                            logger.info(
                                "Throughput for '$componentName': $throughput items/sec (Total: $currentCount)"
                            )

                            // Update last sample values
                            lastSampleTime.set(now)
                            lastItemCount.set(currentCount)
                        }
                    }
                    .onCompletion { exception: Throwable? ->
                        val endTime = Instant.now()
                        val totalTime = Duration.between(startTime, endTime)
                        val totalItems = itemCount.get()
                        val averageThroughput = totalItems.toDouble() / totalTime.seconds

                        logger.info(
                            "Throughput summary for '$componentName': " +
                                    "Total items: $totalItems, " +
                                    "Total time: ${totalTime.seconds}s, " +
                                    "Average throughput: $averageThroughput items/sec"
                        )
                    }
            }
        }
    }
}


