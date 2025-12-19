package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.core.dsl.withLogging
import dev.waylon.apexflow.core.dsl.withTiming
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.tiff.TiffWriter
import dev.waylon.apexflow.tiff.TiffWriterConfig
import java.awt.image.BufferedImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList

/**
 * PDF to TIFF conversion DSL
 * 
 * Provides a simple API for converting PDF files to TIFF format
 * with support for file-based input/output and flexible configurations.
 * 
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * pdfToTiff(
 *     pdfConfig = { dpi = 150f },
 *     tiffConfig = { compressionType = "JPEG" }
 * ).convert(inputFile, outputFile)
 * 
 * // Convert from String path to String path with default settings
 * pdfToTiff().convert("input.pdf", "output.tiff")
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
    
    /**
     * Convert PDF file to TIFF file
     * 
     * @param inputFile Input PDF file
     * @param outputFile Output TIFF file
     */
    suspend fun convert(inputFile: File, outputFile: File) {
        // Create PDF read flow (IO intensive)
        val pdfReadFlow = apexFlow<Pair<File, File>, Pair<File, Flow<BufferedImage>>> {
            transformOnIO { (pdfFile, tiffFile) ->
                val imagesFlow = PdfImageReader(pdfFile, pdfConfig)
                    .read()
                    .withTiming("dev.waylon.apexflow.pdf.reader")
                    .flowOn(Dispatchers.IO)
                
                Pair(tiffFile, imagesFlow)
            }
        }
        .withTiming("PDF Reading Phase")
        .withLogging("PDF Reading")
        
        // Create TIFF write flow (IO intensive)
        val tiffWriteFlow = apexFlow<Pair<File, Flow<BufferedImage>>, Unit> {
            transformOnIO { (tiffFile, imagesFlow) ->
                TiffWriter(tiffFile, tiffConfig)
                    .write(imagesFlow)
            }
        }
        .withTiming("TIFF Writing Phase")
        .withLogging("TIFF Writing")
        
        // Combine flows and execute
        val pdfToTiffFlow = (pdfReadFlow + tiffWriteFlow)
            .withTiming("Total PDF to TIFF Conversion")
            .withLogging("PDF to TIFF Conversion")
        
        pdfToTiffFlow.execute(inputFile to outputFile).collect {  }
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
