package dev.waylon.apexflow.pdf2tiff

import dev.waylon.apexflow.conversion.pdfToTiff
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Edge case tests for PDF to TIFF conversion
 *
 * Tests various error conditions and edge cases including:
 * - Invalid input files
 * - Empty streams
 * - Corrupted data
 * - Exception handling
 * - Invalid configurations
 */
class PdfToTiffEdgeCaseTest {

    // Small test PDF file
    private val smallTestPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")

    /**
     * Test conversion with invalid PDF file
     */
    @Test
    fun `test conversion with invalid pdf file`() = runBlocking {
        // Create a non-PDF file
        val invalidPdfFile = File.createTempFile("invalid", ".pdf").apply {
            writeText("This is not a valid PDF file")
        }

        val outputFile = File.createTempFile("output", ".tiff")

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                pdfToTiff().convert(invalidPdfFile, outputFile)
            }
        }

        // Clean up
        invalidPdfFile.delete()
        outputFile.delete()
    }

    /**
     * Test conversion with empty input stream
     */
    @Test
    fun `test conversion with empty input stream`() = runBlocking {
        // Create empty input stream
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))
        val outputStream = ByteArrayOutputStream()

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                pdfToTiff().convert(emptyInputStream, outputStream)
            }
        }
    }

    /**
     * Test conversion with corrupted PDF data
     */
    @Test
    fun `test conversion with corrupted pdf data`() = runBlocking {
        // Create corrupted PDF data (valid PDF header but corrupted content)
        val corruptedPdfData = ("%PDF-1.7\n" + "".padStart(100, 'x') + "\n%%EOF").toByteArray()
        val corruptedInputStream = ByteArrayInputStream(corruptedPdfData)
        val outputStream = ByteArrayOutputStream()

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                pdfToTiff().convert(corruptedInputStream, outputStream)
            }
        }
    }

    /**
     * Test conversion with non-existent input file
     */
    @Test
    fun `test conversion with non-existent input file`() = runBlocking {
        // Non-existent file
        val nonExistentFile = File("non-existent-file.pdf")
        val outputFile = File.createTempFile("output", ".tiff")

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                pdfToTiff().convert(nonExistentFile, outputFile)
            }
        }

        // Clean up
        outputFile.delete()
    }

    /**
     * Test conversion with invalid DPI configuration
     */
    @Test
    fun `test conversion with invalid dpi configuration`() = runBlocking {
        // Check if test PDF exists
        if (!smallTestPdfFile.exists()) {
            return@runBlocking
        }

        val outputFile = File.createTempFile("output", ".tiff")

        // This should either fail or handle gracefully with minimum DPI
        pdfToTiff(
            pdfConfig = {
                dpi = 0f // Invalid DPI
            }
        ).convert(smallTestPdfFile, outputFile)

        // If it doesn't throw, verify output was created
        assert(outputFile.exists() && outputFile.length() > 0)

        outputFile.delete()
    }

}
