package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.pdf.PdfImageWriterConfig
import dev.waylon.apexflow.tiff.TiffReaderConfig
import dev.waylon.apexflow.tiff.TiffWriterConfig

/**
 * Top-level DSL function for creating a TiffToPdf conversion flow
 *
 * This flow converts TIFF files to PDF files, supporting multi-page documents.
 *
 * Usage Example:
 * ```kotlin
 * val tiffToPdfFlow = tiffToPdf {
 *     tiffReaderConfig {
 *         // Read only specific pages
 *         pageNumbers = listOf(0, 1, 2)
 *         // Skip blank pages
 *         skipBlankPages = true
 *     }
 *     pdfWriterConfig {
 *         // Set JPEG quality for images
 *         jpegQuality = 90f
 *         // Set PDF metadata
 *         metadata {
 *             title = "Converted from TIFF"
 *             author = "ApexFlow"
 *         }
 *     }
 * }
 * ```
 *
 * @param config Configuration block for TiffToPdf conversion
 * @return Configured TiffToPdfFlow instance
 */
@ApexFlowDsl
fun tiffToPdf(config: TiffToPdfConfig.() -> Unit = {}): TiffToPdfFlow {
    return TiffToPdfFlow(config)
}

/**
 * Top-level DSL function for creating a PdfToTiff conversion flow
 *
 * This flow converts PDF files to TIFF files, supporting multi-page documents.
 *
 * Usage Example:
 * ```kotlin
 * val pdfToTiffFlow = pdfToTiff {
 *     pdfReaderConfig {
 *         // Set high DPI for better quality
 *         dpi = 300f
 *         // Use RGB image type
 *         imageType = PdfImageReaderConfig.ImageType.RGB
 *     }
 *     tiffWriterConfig {
 *         // Use LZW compression for lossless quality
 *         compressionType = "LZW"
 *         // Set RGB photometric interpretation
 *         photometricInterpretation = TiffWriterConfig.PhotometricInterpretation.RGB
 *     }
 * }
 * ```
 *
 * @param config Configuration block for PdfToTiff conversion
 * @return Configured PdfToTiffFlow instance
 */
@ApexFlowDsl
fun pdfToTiff(config: PdfToTiffConfig.() -> Unit = {}): PdfToTiffFlow {
    return PdfToTiffFlow(config)
}

/**
 * Top-level DSL function for creating a TiffInputStreamToImages conversion flow
 *
 * This flow converts TIFF input streams to a flow of BufferedImage objects.
 *
 * Usage Example:
 * ```kotlin
 * val tiffImagesFlow = tiffToImages {
 *     // Set DPI for rendering
 *     dpi = 200f
 *     // Read only first 5 pages
 *     pageNumbers = (0..4).toList()
 * }
 * ```
 *
 * @param config Configuration block for TiffInputStreamToImages conversion
 * @return Configured TiffInputStreamToImagesFlow instance
 */
@ApexFlowDsl
fun tiffToImages(config: TiffReaderConfig.() -> Unit = {}): TiffInputStreamToImagesFlow {
    return TiffInputStreamToImagesFlow(config)
}

/**
 * Top-level DSL function for creating a PdfInputStreamToImages conversion flow
 *
 * This flow converts PDF input streams to a flow of BufferedImage objects.
 *
 * Usage Example:
 * ```kotlin
 * val pdfImagesFlow = pdfToImages {
 *     // Set high DPI for better quality
 *     dpi = 300f
 *     // Use grayscale image type for smaller file sizes
 *     imageType = PdfImageReaderConfig.ImageType.GRAY
 *     // Skip blank pages
 *     skipBlankPages = true
 * }
 * ```
 *
 * @param config Configuration block for PdfInputStreamToImages conversion
 * @return Configured PdfInputStreamToImagesFlow instance
 */
@ApexFlowDsl
fun pdfToImages(config: PdfImageReaderConfig.() -> Unit = {}): PdfInputStreamToImagesFlow {
    return PdfInputStreamToImagesFlow(config)
}

/**
 * Top-level DSL function for creating an ImagesToPdf conversion flow
 *
 * This flow converts a flow of BufferedImage objects to PDF files.
 *
 * Usage Example:
 * ```kotlin
 * val imagesToPdfFlow = imagesToPdf {
 *     // Set high JPEG quality
 *     jpegQuality = 95f
 *     // Set PDF version
 *     pdfVersion = "2.0"
 *     // Add metadata
 *     metadata {
 *         title = "Image Collection"
 *         author = "ApexFlow"
 *         keywords = "images, pdf, conversion"
 *     }
 * }
 * ```
 *
 * @param config Configuration block for ImagesToPdf conversion
 * @return Configured ImagesToPdfFlow instance
 */
@ApexFlowDsl
fun imagesToPdf(config: PdfImageWriterConfig.() -> Unit = {}): ImagesToPdfFlow {
    return ImagesToPdfFlow(config)
}

/**
 * Top-level DSL function for creating an ImagesToTiff conversion flow
 *
 * This flow converts a flow of BufferedImage objects to TIFF files.
 *
 * Usage Example:
 * ```kotlin
 * val imagesToTiffFlow = imagesToTiff {
 *     // Use JPEG compression with high quality
 *     compressionType = "JPEG"
 *     compressionQuality = 90f
 *     // Set RGB photometric interpretation
 *     photometricInterpretation = TiffWriterConfig.PhotometricInterpretation.RGB
 * }
 * ```
 *
 * @param config Configuration block for ImagesToTiff conversion
 * @return Configured ImagesToTiffFlow instance
 */
@ApexFlowDsl
fun imagesToTiff(config: TiffWriterConfig.() -> Unit = {}): ImagesToTiffFlow {
    return ImagesToTiffFlow(config)
}
