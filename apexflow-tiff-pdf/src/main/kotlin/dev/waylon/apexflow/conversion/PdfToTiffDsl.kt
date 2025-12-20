package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.withPluginLogging
import dev.waylon.apexflow.core.dsl.withPluginPerformanceMonitoring
import dev.waylon.apexflow.core.dsl.withPluginTiming
import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.tiff.TiffWriter
import dev.waylon.apexflow.tiff.TiffWriterConfig
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
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
 *
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * pdfToTiff(
 *     pdfConfig = { dpi = 150f; skipBlankPages = true },
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
 *
 * // With custom configurations using extension functions
 * inputFile.toTiff(outputFile) {
 *     dpi = 300f
 * } tiffConfig {
 *     compressionType = "LZW"
 * }
 * ```
 */
fun pdfToTiff(
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
): PdfToTiffConverter {
    val readerConfig = PdfImageReaderConfig().apply(pdfConfig)
    val writerConfig = TiffWriterConfig().apply(tiffConfig)
    return PdfToTiffConverter(readerConfig, writerConfig)
}

/**
 * PDF to TIFF converter implementation
 */
class PdfToTiffConverter internal constructor(
    private val pdfConfig: PdfImageReaderConfig,
    private val tiffConfig: TiffWriterConfig
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
        // Delegate to stream version - reduce code duplication
        inputFile.inputStream().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                convert(inputStream, outputStream)
            }
        }
    }

    /**
     * Convert PDF InputStream to TIFF OutputStream using apexFlow DSL
     *
     * This implementation demonstrates apexFlow's core advantages:
     * 1. "Everything is Flow" - All components and operations are Flow-based
     * 2. Reusable components - Create modular, composable flow components
     * 3. Declarative DSL - Intuitive way to compose complex pipelines
     * 4. Clear separation of concerns - Each stage has a single responsibility
     * 5. Easy extensibility - Simple to add new processing steps
     * 6. Reusability - Components can be shared across different pipelines
     *
     * @param inputStream Input PDF InputStream
     * @param outputStream Output TIFF OutputStream
     */
    /**
     * Convert PDF InputStream to TIFF OutputStream using apexFlow DSL
     *
     * This implementation demonstrates the complete "Everything is Flow" principle,
     * including wrapping the writing operation as a Flow component.
     *
     * @param inputStream Input PDF InputStream
     * @param outputStream Output TIFF OutputStream
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        logger.info("Starting PDF to TIFF conversion using apexFlow DSL")

        ///////////////////////////////////////////
        // DEMONSTRATION: EVERYTHING IS FLOW       //
        ///////////////////////////////////////////

        // 1. Create reusable Flow components

        // PDF Reading Flow Component
        val pdfReaderFlow = apexFlow<InputStream, BufferedImage> {
            flatMapMerge { input ->
                PdfImageReader(input, pdfConfig)
                    .read()
                    .withPluginTiming()
                    .flowOn(Dispatchers.IO)
            }
        }

        // Image Processing Flow Component
        val imageProcessorFlow = apexFlow<BufferedImage, BufferedImage> {
            map { image ->
                // Example processing: resize, filter, enhance
                image // Identity for now
            }
        }

        ///////////////////////////////////////////
        // DEMONSTRATION: COMPLETE FLOW PIPELINE    //
        ///////////////////////////////////////////

        // Create input flow - EVERYTHING STARTS AS FLOW

        // Compose complete pipeline by chaining all Flow components
        // This demonstrates the full "Everything is Flow" principle
        val completeImageFlow = pdfReaderFlow + imageProcessorFlow   // Stage 2: Process images
        val resultFlow = completeImageFlow.withPluginTiming()
            .withPluginLogging()
            .withPluginPerformanceMonitoring()
            .execute(inputStream)

        ///////////////////////////////////////////
        // EXECUTION: SINGLE COLLECT CALL         //
        ///////////////////////////////////////////

        // Single collect() call executes the entire pipeline
        // This demonstrates the simplicity and power of Flow execution
        logger.info("Executing complete conversion pipeline")

        // TIFF Writing - EVERYTHING IS FLOW, including writing!
        // Write the complete flow using TiffWriter's Flow support
        logger.info("Writing TIFF file using Flow component")
        val writer = TiffWriter(outputStream, tiffConfig)
        writer.write(resultFlow) // Write the entire flow
        logger.info("Completed TIFF writing using Flow component")

        logger.info("Completed PDF to TIFF conversion")
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
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
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
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
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
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
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
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
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
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
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
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, File(outputPath).outputStream())
}