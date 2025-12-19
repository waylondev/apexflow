package dev.waylon.apexflow.image

/**
 * Image processing constants
 *
 * Holds common constants used across the image processing module
 * to improve code maintainability and consistency.
 */
object ImageConstants {
    /**
     * Default DPI (dots per inch) for PDF rendering
     */
    const val DEFAULT_DPI: Float = 150f

    /**
     * Default JPEG compression quality (0-100)
     */
    const val DEFAULT_JPEG_QUALITY: Float = 85f

    /**
     * Default PDF version
     */
    const val DEFAULT_PDF_VERSION: String = "1.7"

    /**
     * Default TIFF compression quality (0-100)
     */
    const val DEFAULT_TIFF_COMPRESSION_QUALITY: Float = 85f

    /**
     * Default TIFF compression type
     */
    const val DEFAULT_TIFF_COMPRESSION_TYPE: String = "JPEG"

    /**
     * Default TIFF writer producer
     */
    const val DEFAULT_TIFF_PRODUCER: String = "ApexFlow TIFF Writer"

    /**
     * Default PDF writer producer
     */
    const val DEFAULT_PDF_PRODUCER: String = "Apache PDFBox"

    /**
     * Default PDF writer creator
     */
    const val DEFAULT_PDF_CREATOR: String = "ApexFlow PDF Writer"
}
