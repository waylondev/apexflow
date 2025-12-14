package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.dsl.tiffToPdf
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Basic TIFF to PDF converter example
 *
 * This example demonstrates the simplest way to use ApexFlow for TIFF to PDF conversion.
 * It uses default configuration and minimal code.
 */
fun main() {
    val logger = LoggerFactory.getLogger("BasicTiffToPdfConverter")

    // Get input and output paths
    val inputPath = "apexflow-example/build/spring-boot-reference.tiff"
    val outputPath = "apexflow-example/build/spring-boot-reference-output-basic.pdf"

    logger.info("🚀 Starting Basic TIFF to PDF Converter")
    logger.info("📄 Input: $inputPath")
    logger.info("📄 Output: $outputPath")

    // Use try-with-resources to ensure proper resource cleanup
    Files.newInputStream(Paths.get(inputPath)).use { inputStream ->
        Files.newOutputStream(Paths.get(outputPath)).use { outputStream ->
            // Create workflow engine using simplified DSL
            val engine = tiffToPdf(inputStream, outputStream)

            runBlocking {
                // Simplified performance monitoring using withPerformanceMonitoring method
                // pageCount is optional and will be automatically handled
                PerformanceMonitorUtil.withPerformanceMonitoring {
                    // Run the conversion
                    engine.startAsync()
                }
            }
        }
    }

    logger.info("📁 Output file created: $outputPath")
}