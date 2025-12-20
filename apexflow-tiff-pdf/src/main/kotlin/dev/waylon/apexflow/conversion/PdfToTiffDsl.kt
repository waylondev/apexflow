package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.withPluginLogging
import dev.waylon.apexflow.core.dsl.withPluginPerformanceMonitoring
import dev.waylon.apexflow.core.dsl.withPluginTiming
import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.pdf.ApexPdfReader
import dev.waylon.apexflow.pdf.PdfConfig
import dev.waylon.apexflow.tiff.ApexTiffWriter
import dev.waylon.apexflow.tiff.TiffConfig
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * PDF to TIFF conversion DSL
 *
 * Provides a fluent API for converting PDF files to TIFF format
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
 * pdfToTiff(
 *     pdfConfig = { dpi = 150f },
 *     tiffConfig = { compressionType = "JPEG"; compressionQuality = 90f }
 * ).convert(inputFile, outputFile)
 *
 * // Convert from String path to String path with default settings
 * pdfToTiff().convert("input.pdf", "output.tiff")
 *
 * // Convert from InputStream to OutputStream
 * pdfToTiff().convert(inputStream, outputStream)
 *
 * // Use extension functions for concise syntax
 * inputFile.toTiff(outputFile)
 * inputStream.toTiff(outputStream)
 * ```
 */
fun pdfToTiff(
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
): PdfToTiffConverter {
    val readerConfig = PdfConfig().apply(pdfConfig)
    val writerConfig = TiffConfig().apply(tiffConfig)
    return PdfToTiffConverter(readerConfig, writerConfig)
}

/**
 * PDF to TIFF converter implementation
 */
class PdfToTiffConverter internal constructor(
    private val pdfConfig: PdfConfig,
    private val tiffConfig: TiffConfig
) {
    /** Logger instance for this converter */
    private val logger = createLogger<PdfToTiffConverter>()

    /**
     * Convert PDF file to TIFF file
     *
     * @param inputFile Input PDF file
     * @param outputFile Output TIFF file
     */
    suspend fun convert(inputFile: File, outputFile: File) {
        inputFile.inputStream().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                convert(inputStream, outputStream)
            }
        }
    }

    /**
     * Convert PDF file to TIFF file using string paths
     *
     * @param inputPath Input PDF file path
     * @param outputPath Output TIFF file path
     */
    suspend fun convert(inputPath: String, outputPath: String) {
        val inputFile = File(inputPath)
        val outputFile = File(outputPath)
        convert(inputFile, outputFile)
    }

    /**
     * Convert PDF InputStream to TIFF OutputStream using proper ApexFlow design
     *
     * This implementation demonstrates the true "Everything is Flow" principle
     * with proper ApexFlow components that can be composed using the + operator.
     *
     * @param inputStream Input PDF InputStream
     * @param outputStream Output TIFF OutputStream
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        logger.info("Starting PDF to TIFF conversion with ApexFlow components")

        ///////////////////////////////////////////
        // APEXFLOW COMPONENTS                      //
        ///////////////////////////////////////////

        // 1. PDF Reading Component - Flow<Unit> -> Flow<BufferedImage>
        val pdfReader = ApexPdfReader.fromInputStream(inputStream, pdfConfig)

        // 2. Image Processing Component - Flow<BufferedImage> -> Flow<BufferedImage>
        val imageProcessor = apexFlow<BufferedImage, BufferedImage> {
            map { image ->
                // Example processing: resize, filter, enhance
                image // Identity for now
            }
        }

        // 3. TIFF Writing Component - Flow<BufferedImage> -> Flow<Unit>
        val tiffWriter = ApexTiffWriter.toOutputStream(outputStream, tiffConfig)

        ///////////////////////////////////////////
        // FLOW COMPOSITION                         //
        ///////////////////////////////////////////

        // Compose complete pipeline using + operator - TRUE APEXFLOW COMPOSITION
        val completePipeline = pdfReader + imageProcessor + tiffWriter

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

        logger.info("Completed PDF to TIFF conversion with ApexFlow")
    }
}

/**
 * Extension function: Convert File to TIFF with DSL configuration
 *
 * @param outputFile Output TIFF file
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun File.toTiff(
    outputFile: File,
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, outputFile)
}

/**
 * Extension function: Convert File to TIFF with output stream
 *
 * @param outputStream Output TIFF stream
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun File.toTiff(
    outputStream: OutputStream,
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this.inputStream(), outputStream)
}

/**
 * Extension function: Convert File to TIFF with string path
 *
 * @param outputPath Output TIFF file path
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun File.toTiff(
    outputPath: String,
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, File(outputPath))
}

/**
 * Extension function: Convert InputStream to TIFF with output file
 *
 * @param outputFile Output TIFF file
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun InputStream.toTiff(
    outputFile: File,
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, outputFile.outputStream())
}

/**
 * Extension function: Convert InputStream to TIFF with output stream
 *
 * @param outputStream Output TIFF stream
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun InputStream.toTiff(
    outputStream: OutputStream,
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, outputStream)
}

/**
 * Extension function: Convert InputStream to TIFF with string path
 *
 * @param outputPath Output TIFF file path
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun InputStream.toTiff(
    outputPath: String,
    pdfConfig: PdfConfig.() -> Unit = {},
    tiffConfig: TiffConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, File(outputPath).outputStream())
}