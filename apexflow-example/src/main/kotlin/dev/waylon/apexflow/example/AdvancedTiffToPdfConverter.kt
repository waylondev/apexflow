package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.tiff.TiffReader
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Advanced TIFF to PDF converter example
 *
 * This example demonstrates advanced usage of ApexFlow:
 * 1. Custom image processing during conversion
 * 2. Performance-optimized configuration
 */
fun main() {
    val logger = LoggerFactory.getLogger("AdvancedTiffToPdfConverter")
    // Get input and output paths
    val inputPath = "apexflow-example/build/spring-boot-reference.tiff"
    val outputPath = "apexflow-example/build/spring-boot-reference-output-advanced.pdf"

    logger.info("🚀 Starting Advanced TIFF to PDF Converter")
    logger.info("📄 Input: $inputPath")
    logger.info("📄 Output: $outputPath")

    // Create custom processor with watermark functionality
    val watermarkProcessor = workflowProcessor()

    // Use try-with-resources to ensure proper resource cleanup
    FileInputStream(inputPath).use { inputStream ->
        FileOutputStream(outputPath).use { outputStream ->
            // Create workflow engine using DSL with custom processor and configuration
            // Use configure block for clearer separation of configuration
            val engine = apexFlowWorkflow {
                reader(TiffReader(inputStream))
                processor(watermarkProcessor)
                writer(PdfImageWriter(outputStream))

                // Use configure block for clear configuration separation
                configure {
                    readBufferSize = 1000
                    processBufferSize = 1000
                    ioBufferSize = 8 * 8192
                    // readDispatcher, processDispatcher, writeDispatcher use default values
                }
            }

            // Use the simplified performance monitoring method
            runBlocking {
                PerformanceMonitorUtil.withPerformanceMonitoring {
                    // Run the conversion within the performance monitoring block
                    engine.startAsync()
                }
            }
        }
    }

    logger.info("📁 Output file created: $outputPath")
    logger.info("🔧 Configuration: Performance Optimized")
    logger.info("✨ Features:")
    logger.info("   - Custom image processing with watermark")
    logger.info("   - Performance-optimized settings")
}

private fun workflowProcessor(): WorkflowProcessor<BufferedImage, BufferedImage> {
    val watermarkProcessor = object : WorkflowProcessor<BufferedImage, BufferedImage> {
        override fun process(input: Flow<BufferedImage>): Flow<BufferedImage> {
            return input.map { image ->
                // Create a copy of the image to modify
                val processedImage = BufferedImage(
                    image.width,
                    image.height,
                    BufferedImage.TYPE_INT_RGB
                )

                // Draw the original image
                val graphics = processedImage.createGraphics()
                graphics.drawImage(image, 0, 0, null)

                // Add a simple watermark (for demonstration purposes)
                graphics.color = Color(255, 255, 255, 128) // Semi-transparent white
                graphics.font = graphics.font.deriveFont(16f)
                graphics.drawString("ApexFlow", 10, image.height - 20)

                graphics.dispose()

                processedImage
            }
        }
    }
    return watermarkProcessor
}