package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.dsl.pdfToTiff
import kotlinx.coroutines.runBlocking

/**
 * PDF to TIFF Converter Example with Comprehensive Performance Monitoring
 *
 * Demonstrates how to use ApexFlow simplified DSL to convert PDF files to TIFF files
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

    // Create workflow engine using simplified ApexFlow DSL
    val engine = pdfToTiff(inputPath, outputPath)

    // Use the simplified performance monitoring method
    runBlocking {
        PerformanceMonitorUtil.withPerformanceMonitoring {
            // Execute workflow - PDF to TIFF conversion
            engine.startAsync()
        }
    }

    println("📁 Output file created: $outputPath")
    println("🚀 Performance test completed!")

    // Note: Workflow metrics are logged internally by ApexFlow
    println("📈 Workflow metrics available in console output")
}