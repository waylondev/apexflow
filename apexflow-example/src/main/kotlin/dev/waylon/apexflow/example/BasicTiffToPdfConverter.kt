package dev.waylon.apexflow.example

import dev.waylon.apexflow.core.util.PerformanceMonitorUtil
import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.core.workflow.noOp
import dev.waylon.apexflow.pdf.PdfWriter
import dev.waylon.apexflow.tiff.TiffReader
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
    val inputPath = "apexflow-example/build/spring-boot-reference.tif"
    val outputPath = "apexflow-example/build/spring-boot-reference-output-basic.pdf"

    logger.info("🚀 Starting Basic TIFF to PDF Converter")
    logger.info("📄 Input: $inputPath")
    logger.info("📄 Output: $outputPath")

    // Create workflow engine using DSL - processor is optional (defaults to identity)
    val engine = apexFlowWorkflow {
        reader(TiffReader(inputPath = inputPath))
        processor(WorkflowProcessor.noOp())
        writer(PdfWriter(outputPath))
        configure {
        }
    }

    runBlocking {
        // Simplified performance monitoring using withPerformanceMonitoring method
        // pageCount is optional and will be automatically handled
        PerformanceMonitorUtil.withPerformanceMonitoring(logger) {
            // Run the conversion
            engine.startAsync()
        }
    }

    logger.info("📁 Output file created: $outputPath")


}