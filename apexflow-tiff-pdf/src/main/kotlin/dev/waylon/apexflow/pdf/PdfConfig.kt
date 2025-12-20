package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.image.ImageConstants

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
    var jpegQuality: Float = 0.95f

    /** PDF page size */
    var pageSize: org.apache.pdfbox.pdmodel.common.PDRectangle = org.apache.pdfbox.pdmodel.common.PDRectangle.A4

    /**
     * Buffer size for flow processing
     * 0: Disable buffering (true streaming)
     * >0: Use specified buffer size
     * Default: 0 (disabled) for true streaming processing
     */
    var bufferSize: Int = 0
}