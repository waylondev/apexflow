package dev.waylon.apexflow.conversion

import java.io.File
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Performance tests for PDF and TIFF conversion
 *
 * These tests focus on optimal performance configurations
 * for converting large files using ApexFlow components.
 */
class PdfToTiffTest {
    // Use existing test PDF
    val inputPdf = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
    val outputTiff = File("build/test-pdf-to-tiff-dsl-406-output.tiff")

    /**
     * Test optimal PDF to TIFF conversion performance
     * using extension functions for simplified code
     */
    @Test
    fun `test optimal pdf to tiff performance`() = runBlocking {

        println("Starting optimal PDF to TIFF conversion test...")

        // Measure conversion time
        val timeMillis = measureTimeMillis {
            // Use extension function for simplified call
            inputPdf.toTiff(
                outputTiff,
                pdfConfig = {
                    dpi = 100f // Balanced resolution for speed and quality
//                    bufferSize = 5
                },
                tiffConfig = {
                    compressionType = "JPEG" // Fast compression
                    compressionQuality = 70f // Balanced quality for speed
                }
            )
        }

        println("PDF to TIFF conversion completed in $timeMillis ms")
        println("Output file size: ${outputTiff.length() / 1024 / 1024} MB")

        // Clean up
//        outputTiff.delete()
    }


}