package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Configured TIFF to PDF converter example
 *
 * This example demonstrates how to use custom configuration with ApexFlow.
 * It shows how to adjust buffer sizes and enable performance monitoring.
 */
fun main() {
    val logger = LoggerFactory.getLogger("ConfiguredTiffToPdfConverter")
    // Get input and output paths
    val inputPath = "apexflow-example/build/spring-boot-reference.pdf"
    val outputPath = "apexflow-example/build/spring-boot-reference-output-configured.tiff"

    logger.info("🚀 Starting Configured PDF to TIFF Converter with Performance Monitoring")
    logger.info("📄 Input: $inputPath")
    logger.info("📄 Output: $outputPath")
    logger.info("📊 Performance monitoring enabled")

    // Use try-with-resources to ensure proper resource cleanup
    FileInputStream(inputPath).use { inputStream ->
        FileOutputStream(outputPath).use { outputStream ->
            // Create workflow engine using DSL with custom configuration
            // Use configure block but only set the properties we care about, others use default values
            val engine = apexFlowWorkflow {
                reader(PdfImageReader(inputStream))
                processor(WorkflowProcessor.identity())
                writer(TiffWriter(outputStream) {
                    val writer = ImageIO.getImageWritersByFormatName("tiff").next()
                    writeParam = writer.defaultWriteParam
                    writeParam?.compressionMode = ImageWriteParam.MODE_EXPLICIT
                    writeParam?.compressionType = "JPEG"
                    writeParam?.compressionQuality = 90f / 100f

                })
                // Use configure block, only set the properties we care about
                configure {
                    readBufferSize = 500
                    processBufferSize = 500
                    ioBufferSize = 4 * 8192
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
    logger.info("🔧 Configuration:")
    logger.info("   - Read Buffer Size: 500")
    logger.info("   - Process Buffer Size: 500")
    logger.info("   - Performance Monitoring: true")
    logger.info("   - IO Buffer Size: ${4 * 8192}")
    logger.info("   - Granular Performance Monitoring: true")
}