package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.WorkflowReader
import dev.waylon.apexflow.core.workflow.WorkflowWriter
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Number Processing Example using DSL
 *
 * Demonstrates that ApexFlow is not limited to file I/O, but can handle any type of data flow.
 *
 * This example uses the ApexFlow DSL to create a number processing workflow:
 * 1. Generates a flow of 10 million numbers
 * 2. Processes each number by multiplying it by 2
 * 3. Outputs the results (logs a sample of the results)
 * 4. Uses built-in performance monitoring for execution statistics
 */
fun main() {
    val logger = LoggerFactory.getLogger("NumberProcessingExample")
    val numberCount = 10_000L // 10 million numbers

    logger.info("🚀 Starting Number Processing Example with DSL")
    logger.info("📊 Processing $numberCount numbers...")
    logger.info("🏗️  Using ApexFlow DSL for workflow construction")

    // Create components using DSL
    val processor = MultiplyByTwoProcessor(logger)
    val writer = NumberWriter(logger)

    // Create workflow engine using simplified DSL
    val engine = apexFlowWorkflow {
        // Create a custom reader that generates numbers
        reader(NumberReader(numberCount, logger))
        processor(processor)
        writer(writer)

        // Configure workflow for optimal performance
        configure {
            readBufferSize = 10_000
            processBufferSize = 10_000
        }
    }

    // Run the workflow with built-in performance monitoring
    runBlocking {
        logger.info("📈 Starting performance monitoring")
        PerformanceMonitorUtil.withPerformanceMonitoring(numberCount.toInt()) {  // Use actual number count for metrics
            // Run the workflow
            logger.info("▶️  Starting workflow execution")
            engine.startAsync()
            logger.info("✅ Workflow execution completed")
        }
    }
}

/**
 * Custom reader that generates a flow of numbers with logging
 */
class NumberReader(private val count: Long, private val logger: org.slf4j.Logger) : WorkflowReader<Long> {
    override fun read(): Flow<Long> {
        return flow {
            val coroutineName = currentCoroutineContext().toString()
            logger.info("📖 NumberReader starting on coroutine: $coroutineName")
            logger.info("📊 Generating $count numbers")

            // Generate numbers in chunks to avoid memory issues
            val chunkSize = 100_000L
            var remaining = count
            var current = 1L

            while (remaining > 0) {
                val chunk = minOf(chunkSize, remaining)
                logger.debug("📦 Generating chunk of $chunk numbers on coroutine: $coroutineName")

                // Emit numbers in this chunk
                for (i in 0 until chunk) {
                    emit(current + i)
                }

                current += chunk
                remaining -= chunk
            }

            logger.info("✅ NumberReader completed on coroutine: $coroutineName")
        }
    }
}

/**
 * Custom processor that multiplies each number by 2 with logging
 */
class MultiplyByTwoProcessor(private val logger: org.slf4j.Logger) : WorkflowProcessor<Long, Long> {
    override fun process(input: Flow<Long>): Flow<Long> {
        return input.map {
            val coroutineName = currentCoroutineContext().toString()

            // Log every 10 million numbers to avoid excessive logging
            if (it % 10_000 == 0L) {
                logger.debug("⚙️  Processing number $it on coroutine: $coroutineName")
            }

            it * 2
        }
    }
}

/**
 * Custom writer that logs results (samples every 1 million numbers)
 */
class NumberWriter(private val logger: org.slf4j.Logger) : WorkflowWriter<Long> {
    override suspend fun write(data: Flow<Long>) {
        val coroutineName = currentCoroutineContext().toString()
        logger.info("📝 NumberWriter starting on coroutine: $coroutineName")

        var counter = 0L
        data.collect {
            counter++

            // Log a sample of the results (every 1 million numbers)
            if (counter % 1_000_000 == 0L) {
                logger.info("🔢 Processed $counter numbers: ${counter / 2} * 2 = $it")
                logger.debug("📊 Processing on coroutine: $coroutineName")
            }
        }

        logger.info("✅ NumberWriter completed on coroutine: $coroutineName")
        logger.info("📊 Total numbers processed: $counter")
    }
}
