package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.image.ImageConstants
import javax.imageio.ImageWriteParam

/**
 * TIFF configuration for both reading and writing
 *
 * Provides centralized configuration for all TIFF-related operations
 */
class TiffConfig {
    /** Image write parameters for customizing TIFF writing behavior */
    var writeParam: ImageWriteParam? = null

    /** Compression type to use */
    var compressionType: String = ImageConstants.DEFAULT_TIFF_COMPRESSION_TYPE

    /** Compression quality (0-100) */
    var compressionQuality: Float = ImageConstants.DEFAULT_TIFF_COMPRESSION_QUALITY

    /** Whether to write pages in parallel */
    var parallelWriting: Boolean = false
}