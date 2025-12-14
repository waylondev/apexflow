package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.dsl.pdfToTiff
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

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
    val logger = LoggerFactory.getLogger("PdfToTiffConverter")
    
    // Configure input and output paths
    val inputPath = "apexflow-example/build/spring-boot-reference.pdf"
    val outputPath = "apexflow-example/build/spring-boot-reference.tif"

    logger.info("🚀 Starting PDF to TIFF Converter with Comprehensive Performance Monitoring")
    logger.info("📄 Input: {}", inputPath)
    logger.info("📄 Output: {}", outputPath)
    logger.info("📊 Performance monitoring enabled")

    // Use try-with-resources to ensure proper resource cleanup
    FileInputStream(inputPath).use { inputStream ->
        FileOutputStream(outputPath).use { outputStream ->
            // Create workflow engine using simplified ApexFlow DSL
            val engine = pdfToTiff(inputStream, outputStream)

            // Use the simplified performance monitoring method
            runBlocking {
                PerformanceMonitorUtil.withPerformanceMonitoring {
                    // Execute workflow - PDF to TIFF conversion
                    engine.startAsync()
                }
            }
        }
    }

    logger.info("📁 Output file created: {}", outputPath)
    logger.info("🚀 Performance test completed!")

    // Note: Workflow metrics are logged internally by ApexFlow
    logger.info("📈 Workflow metrics available in console output")
}