package dev.waylon.apexflow.pdf2tiff

import dev.waylon.apexflow.conversion.pdfToTiff
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Test case for converting a specific PDF file to TIFF
 *
 * This test converts the test-pdf-to-tiff-dsl.pdf file from resources to TIFF
 */
class PdfToTiffConversionTest {

    // Define input and output file paths
    private val inputPdf = File("dist/test-pdf-to-tiff-dsl.pdf")
    private val outputTiff = File("build/test-pdf-to-tiff-dsl.tiff")

    @BeforeEach
    fun setUp() {
        // Ensure output directory exists
        outputTiff.parentFile?.mkdirs()
    }

    @AfterEach
    fun tearDown() {
        // Clean up test files
        outputTiff.delete()
    }

    /**
     * Test converting PDF to TIFF using DSL
     *
     * Steps:
     * 1. Read PDF file (IO intensive)
     * 2. Parse PDF to get image flow (CPU intensive)
     * 3. Process images (CPU intensive)
     * 4. Write TIFF file (IO intensive)
     */
    @Test
    fun `test converting pdf to tiff`() = runBlocking {
        // Check if input file exists
        assertTrue(inputPdf.exists(), "Input PDF file does not exist: ${inputPdf.absolutePath}")

        // Use the simplified PDF to TIFF conversion DSL
        pdfToTiff(
            pdfConfig = { dpi = 100f },
            tiffConfig = {
                compressionType = "JPEG"
                compressionQuality = 90f
            }
        ).convert(inputPdf, outputTiff)

        // Verify that output file was created
        assertTrue(outputTiff.exists(), "Output TIFF file was not created")
        assertTrue(outputTiff.length() > 0, "Output TIFF file is empty")
    }
}