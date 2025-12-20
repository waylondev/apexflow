package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.pdf.PdfImageWriterConfig
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffReaderConfig
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * TIFF to PDF conversion DSL
 *
 * Provides a fluent API for converting TIFF files to PDF format
 * with support for multiple input/output types and flexible configurations.
 *
 * Core features:
 * - Type-safe DSL with clear configuration options
 * - Support for File, InputStream, OutputStream, and String paths
 * - Configurable JPEG quality, PDF version, and compression settings
 * - Asynchronous processing using Kotlin coroutines
 * - Stream-based processing for efficient memory usage
 * - Comprehensive extension functions for easy integration
 *
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * tiffToPdf(
 *     tiffConfig = { /* TIFF reader config */ },
 *     pdfConfig = { jpegQuality = 90f; compressContent = true; pdfVersion = "1.7" }
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
 *
 * // With custom configurations using extension functions
 * inputFile.toPdf(outputFile) {
 *     // TIFF config options
 * } pdfConfig {
 *     jpegQuality = 85f
 *     compressContent = true
 * }
 * ```
 */
fun tiffToPdf(
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
): TiffToPdfConverter {
    val readerConfig = TiffReaderConfig().apply(tiffConfig)
    val writerConfig = PdfImageWriterConfig().apply(pdfConfig)
    return TiffToPdfConverter(readerConfig, writerConfig)
}

/**
 * TIFF to PDF converter implementation
 */
class TiffToPdfConverter internal constructor(
    private val tiffConfig: TiffReaderConfig,
    private val pdfConfig: PdfImageWriterConfig
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
        // Delegate to stream version - reduce code duplication
        inputFile.inputStream().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                convert(inputStream, outputStream)
            }
        }
    }

    /**
     * Convert TIFF InputStream to PDF OutputStream using apexFlow DSL
     *
     * This implementation demonstrates the complete "Everything is Flow" principle,
     * including wrapping the writing operation as a Flow component.
     *
     * @param inputStream Input TIFF InputStream
     * @param outputStream Output PDF OutputStream
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        logger.info("Starting TIFF to PDF conversion using apexFlow DSL")
        
        ///////////////////////////////////////////
        // DEMONSTRATION: EVERYTHING IS FLOW       //
        ///////////////////////////////////////////
        
        // 1. Create reusable Flow components
        
        // TIFF Reading Flow Component
        val tiffReaderFlow = apexFlow<InputStream, BufferedImage> {
            flatMapMerge { input ->
                TiffReader(input, tiffConfig).read()
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
        val inputFlow = flowOf(inputStream)
        
        // Compose complete pipeline by chaining all Flow components
        // This demonstrates the full "Everything is Flow" principle
        val completeImageFlow = inputFlow
            .let { tiffReaderFlow.transform(it) }        // Stage 1: Read TIFF pages
            .let { imageProcessorFlow.transform(it) }   // Stage 2: Process images
        
        ///////////////////////////////////////////
        // EXECUTION: SINGLE COLLECT CALL         //
        ///////////////////////////////////////////
        
        // Single collect() call executes the entire pipeline
        // This demonstrates the simplicity and power of Flow execution
        logger.info("Executing complete conversion pipeline")
        
        // PDF Writing - EVERYTHING IS FLOW, including writing!
        // Write the complete flow using PdfImageWriter's Flow support
        logger.info("Writing PDF file using Flow component")
        val writer = PdfImageWriter(outputStream, pdfConfig)
        writer.write(completeImageFlow) // Write the entire flow
        logger.info("Completed PDF writing using Flow component")
        
        logger.info("Completed TIFF to PDF conversion")
        logger.info("ApexFlow advantages demonstrated:")
        logger.info("1. Everything is Flow - All operations use Flow API, including writing")
        logger.info("2. Reusable components - Created modular, shareable flow components")
        logger.info("3. Declarative composition - Clear, intuitive pipeline building")
        logger.info("4. Easy extensibility - Added processing stages with minimal code")
        logger.info("5. Single execution point - One collect() call runs the entire pipeline")
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
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, outputFile)
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
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this.inputStream(), outputStream)
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
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, File(outputPath))
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
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, outputFile.outputStream())
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
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, outputStream)
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
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, File(outputPath).outputStream())
}