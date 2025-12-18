package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.pdf.PdfImageWriterConfig
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffReaderConfig
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

/**
 * Configuration for TiffToPdf conversion
 */
class TiffToPdfConfig {
    /** Configuration for TIFF reading */
    var tiffReaderConfig: (TiffReaderConfig.() -> Unit) = {}

    /** Configuration for PDF writing */
    var pdfWriterConfig: (PdfImageWriterConfig.() -> Unit) = {}
}

/**
 * ApexFlow implementation for converting TIFF to PDF
 *
 * This flow reads TIFF images from an InputStream and writes them to a PDF OutputStream
 *
 * Usage:
 * ```kotlin
 * val tiffToPdfFlow = TiffToPdfFlow {
 *     tiffReaderConfig {
 *         // TIFF reader configuration
 *     }
 *     pdfWriterConfig {
 *         jpegQuality = 90f
 *     }
 * }
 * ```
 */
class TiffToPdfFlow(
    private val config: TiffToPdfConfig.() -> Unit = {}
) : ApexFlow<Pair<InputStream, OutputStream>, Unit> {

    private val conversionConfig = TiffToPdfConfig().apply(config)

    override fun transform(input: Flow<Pair<InputStream, OutputStream>>): Flow<Unit> {
        return input.map { (tiffInputStream, pdfOutputStream) ->
            val tiffReader = TiffReader(tiffInputStream, conversionConfig.tiffReaderConfig)
            val pdfWriter = PdfImageWriter(pdfOutputStream, conversionConfig.pdfWriterConfig)

            val images = tiffReader.read()
            pdfWriter.write(images)
        }
    }
}

/**
 * ApexFlow implementation for converting TIFF InputStream to Flow<BufferedImage>
 */
class TiffInputStreamToImagesFlow(
    private val config: TiffReaderConfig.() -> Unit = {}
) : ApexFlow<InputStream, BufferedImage> {

    override fun transform(input: Flow<InputStream>): Flow<BufferedImage> {
        return input.flatMapConcat { stream ->
            TiffReader(stream, config).read()
        }
    }
}

/**
 * ApexFlow implementation for converting Flow<BufferedImage> to PDF OutputStream
 */
class ImagesToPdfFlow(
    private val config: PdfImageWriterConfig.() -> Unit = {}
) : ApexFlow<Pair<Flow<BufferedImage>, OutputStream>, Unit> {

    override fun transform(input: Flow<Pair<Flow<BufferedImage>, OutputStream>>): Flow<Unit> {
        return input.map { (images, outputStream) ->
            val pdfWriter = PdfImageWriter(outputStream, config)
            pdfWriter.write(images)
        }
    }
}