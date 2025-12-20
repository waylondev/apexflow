package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.withPluginLogging
import dev.waylon.apexflow.core.dsl.withPluginPerformanceMonitoring
import dev.waylon.apexflow.core.dsl.withPluginTiming
import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.pdf.ApexPdfWriter
import dev.waylon.apexflow.pdf.PdfConfig
import dev.waylon.apexflow.tiff.ApexTiffReader
import dev.waylon.apexflow.tiff.TiffConfig
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * TIFF to PDF conversion DSL
 *
 * Provides a fluent API for converting TIFF files to PDF format
 * with support for multiple input/output types and flexible configurations.
 *
 * Core features:
 * - Type-safe DSL with clear configuration options
 * - Support for File, InputStream, OutputStream, and String paths
 * - Configurable DPI, compression, and other conversion settings
 * - Asynchronous processing using Kotlin coroutines
 * - Stream-based processing for efficient memory usage
 * - Comprehensive extension functions for easy integration
 * - True ApexFlow "Everything is Flow" design
 * - Direct implementation of ApexFlow interface
 *
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * tiffToPdf(
 *     tiffConfig = { /* TIFF config */ },
 *     pdfConfig = { dpi = 300f; jpegQuality = 0.95f }
 * ).convert(inputFile, outputFile)
 *
 * // Convert from String path to String path with default settings
 * tiffToPdf().convert("input.tiff", "output.pdf")
 *
 * // Convert from InputStream to OutputStream
 * tiffToPdf().convert(inputStream, outputStream)
 *
 * // Use extension functions for concise syntax
 * inputFile.toPdf(outputFile)
 * inputStream.toPdf(outputStream)
 * ```
 */
fun apexTiffToPdf(
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
): TiffToPdfConverter {
    val readerConfig = TiffConfig().apply(tiffConfig)
    val writerConfig = PdfConfig().apply(pdfConfig)
    return TiffToPdfConverter(readerConfig, writerConfig)
}

/**
 * TIFF to PDF converter implementation
 */
class TiffToPdfConverter internal constructor(
    private val tiffConfig: TiffConfig,
    private val pdfConfig: PdfConfig
) {
    /** Logger instance for this converter */
    private val logger = createLogger<TiffToPdfConverter>()

    /**
     * Convert TIFF file to PDF file
     *
     * @param inputFile Input TIFF file
     * @param outputFile Output PDF file
     */
    suspend fun convert(inputFile: File, outputFile: File) {
        inputFile.inputStream().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                convert(inputStream, outputStream)
            }
        }
    }

    /**
     * Convert TIFF file to PDF file using string paths
     *
     * @param inputPath Input TIFF file path
     * @param outputPath Output PDF file path
     */
    suspend fun convert(inputPath: String, outputPath: String) {
        val inputFile = File(inputPath)
        val outputFile = File(outputPath)
        convert(inputFile, outputFile)
    }

    /**
     * Convert TIFF InputStream to PDF OutputStream using proper ApexFlow design
     *
     * This implementation demonstrates the true "Everything is Flow" principle
     * with proper ApexFlow components that can be composed using the + operator.
     *
     * @param inputStream Input TIFF InputStream
     * @param outputStream Output PDF OutputStream
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        logger.info("Starting TIFF to PDF conversion with ApexFlow components")

        ///////////////////////////////////////////
        // APEXFLOW COMPONENTS                      //
        ///////////////////////////////////////////

        // 1. TIFF Reading Component - Flow<Unit> -> Flow<BufferedImage>
        val tiffReader = ApexTiffReader.fromInputStream(inputStream, tiffConfig)

        // 2. Image Processing Component - Flow<BufferedImage> -> Flow<BufferedImage>
        val imageProcessor = apexFlow<BufferedImage, BufferedImage> {
            map { image ->
                // Example processing: resize, filter, enhance
                image // Identity for now
            }
        }

        // 3. PDF Writing Component - Flow<BufferedImage> -> Flow<Unit>
        val pdfWriter = ApexPdfWriter.toOutputStream(outputStream, pdfConfig)

        ///////////////////////////////////////////
        // FLOW COMPOSITION                         //
        ///////////////////////////////////////////

        // Compose complete pipeline using + operator - TRUE APEXFLOW COMPOSITION
        val completePipeline = tiffReader + imageProcessor + pdfWriter

        // Apply plugins to the complete pipeline
        val pipelineWithPlugins = completePipeline.withPluginTiming()
            .withPluginLogging()
            .withPluginPerformanceMonitoring()

        ///////////////////////////////////////////
        // EXECUTION                                 //
        ///////////////////////////////////////////

        // Execute the complete pipeline with a trigger flow
        val triggerFlow = flowOf(Unit)

        logger.info("Executing ApexFlow pipeline")

        // Execute the pipeline
        pipelineWithPlugins.execute(triggerFlow)

        logger.info("Completed TIFF to PDF conversion with ApexFlow")
    }
}

/**
 * Extension function: Convert File to PDF with DSL configuration
 *
 * @param outputFile Output PDF file
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun File.toPdf(
    outputFile: File,
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
) {
    apexTiffToPdf(tiffConfig, pdfConfig).convert(this, outputFile)
}

/**
 * Extension function: Convert File to PDF with output stream
 *
 * @param outputStream Output PDF stream
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun File.toPdf(
    outputStream: OutputStream,
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
) {
    apexTiffToPdf(tiffConfig, pdfConfig).convert(this.inputStream(), outputStream)
}

/**
 * Extension function: Convert File to PDF with string path
 *
 * @param outputPath Output PDF file path
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun File.toPdf(
    outputPath: String,
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
) {
    apexTiffToPdf(tiffConfig, pdfConfig).convert(this, File(outputPath))
}

/**
 * Extension function: Convert InputStream to PDF with output file
 *
 * @param outputFile Output PDF file
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun InputStream.toPdf(
    outputFile: File,
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
) {
    apexTiffToPdf(tiffConfig, pdfConfig).convert(this, outputFile.outputStream())
}

/**
 * Extension function: Convert InputStream to PDF with output stream
 *
 * @param outputStream Output PDF stream
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun InputStream.toPdf(
    outputStream: OutputStream,
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
) {
    apexTiffToPdf(tiffConfig, pdfConfig).convert(this, outputStream)
}

/**
 * Extension function: Convert InputStream to PDF with string path
 *
 * @param outputPath Output PDF file path
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun InputStream.toPdf(
    outputPath: String,
    tiffConfig: TiffConfig.() -> Unit = {},
    pdfConfig: PdfConfig.() -> Unit = {}
) {
    apexTiffToPdf(tiffConfig, pdfConfig).convert(this, File(outputPath).outputStream())
}