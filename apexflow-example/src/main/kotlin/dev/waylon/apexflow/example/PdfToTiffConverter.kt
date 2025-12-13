package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.tiff.TiffWriter
import kotlinx.coroutines.runBlocking

/**
 * PDF to TIFF Converter Example with Comprehensive Performance Monitoring
 *
 * Demonstrates how to use ApexFlow DSL to convert PDF files to TIFF files
 * with comprehensive performance monitoring including:
 * - Memory usage (heap and non-heap)
 * - CPU usage
 * - Garbage collection statistics
 * - System load
 * - Conversion speed
 */
fun main() {
    // Configure input and output paths
    val inputPath = "apexflow-example/build/spring-boot-reference.pdf"
    val outputPath = "apexflow-example/build/spring-boot-reference.tif"

    println("🚀 Starting PDF to TIFF Converter with Comprehensive Performance Monitoring")
    println("📄 Input: $inputPath")
    println("📄 Output: $outputPath")
    println("📊 Performance monitoring enabled")

    // Create workflow engine using ApexFlow DSL
    val engine = apexFlowWorkflow {
        // Configure PdfReader with optimal settings for smaller file size
        reader(
            PdfImageReader(
                inputPath = inputPath,
            )
        )
        processor(WorkflowProcessor.identity())
        writer(TiffWriter(outputPath))
        configure {
            readBufferSize = 500
            processBufferSize = 500
            ioBufferSize = 4 * 8192
        }
    }

    // Use the simplified performance monitoring method
    runBlocking {
        PerformanceMonitorUtil.withPerformanceMonitoring() {
            // Execute workflow - PDF to TIFF conversion
            engine.startAsync()
        }
    }

    println("📁 Output file created: $outputPath")
    println("🚀 Performance test completed!")

    // Note: Workflow metrics are logged internally by ApexFlow
    println("📈 Workflow metrics available in console output")
}