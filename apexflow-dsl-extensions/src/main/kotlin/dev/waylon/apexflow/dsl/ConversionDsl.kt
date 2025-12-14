package dev.waylon.apexflow.dsl

import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.awt.image.BufferedImage

/**
 * Simplified TIFF to PDF conversion
 *
 * This function provides a concise way to create a TIFF to PDF conversion workflow
 * with minimal configuration.
 *
 * @param inputPath Path to the input TIFF file
 * @param outputPath Path to the output PDF file
 * @return Configured workflow engine ready to execute
 *
 * Usage example:
 * ```kotlin
 * val engine = tiffToPdf("input.tif", "output.pdf")
 * runBlocking { engine.startAsync() }
 * ```
 */
fun tiffToPdf(
    inputPath: String,
    outputPath: String
) = apexFlowWorkflow {
    reader(TiffReader(inputPath = inputPath))
    processor(WorkflowProcessor.identity())
    writer(PdfImageWriter(outputPath))
}

/**
 * Simplified PDF to TIFF conversion
 *
 * This function provides a concise way to create a PDF to TIFF conversion workflow
 * with minimal configuration.
 *
 * @param inputPath Path to the input PDF file
 * @param outputPath Path to the output TIFF file
 * @return Configured workflow engine ready to execute
 *
 * Usage example:
 * ```kotlin
 * val engine = pdfToTiff("input.pdf", "output.tif")
 * runBlocking { engine.startAsync() }
 * ```
 */
fun pdfToTiff(
    inputPath: String,
    outputPath: String
) = apexFlowWorkflow {
    reader(PdfImageReader(inputPath = inputPath))
    processor(WorkflowProcessor.identity())
    writer(TiffWriter(outputPath))
}
