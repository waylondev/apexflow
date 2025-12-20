package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.image.ImageConstants
import org.apache.pdfbox.pdmodel.common.PDRectangle

/**
 * PDF configuration for both reading and writing
 *
 * Provides centralized configuration for all PDF-related operations
 */
class PdfConfig {
    /** DPI for PDF rendering */
    var dpi: Float = ImageConstants.DEFAULT_DPI

    /** Skip blank pages during rendering */
    var skipBlankPages: Boolean = false

    /** JPEG compression quality for PDF writing (0-1) */
    var jpegQuality: Float = ImageConstants.DEFAULT_JPEG_QUALITY

    /** PDF page size */
    var pageSize: PDRectangle = PDRectangle.A4
}