package dev.waylon.apexflow.tiff2pdf

import dev.waylon.apexflow.conversion.tiffToPdf
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Edge case tests for TIFF to PDF conversion
 *
 * Tests various error conditions and edge cases including:
 * - Invalid input files
 * - Empty streams
 * - Corrupted data
 * - Exception handling
 * - Invalid configurations
 */
class TiffToPdfEdgeCaseTest {

    /**
     * Test conversion with invalid TIFF file
     */
    @Test
    fun `test conversion with invalid tiff file`() = runBlocking {
        // Create a non-TIFF file
        val invalidTiffFile = File.createTempFile("invalid", ".tiff").apply {
            writeText("This is not a valid TIFF file")
        }

        val outputFile = File.createTempFile("output", ".pdf")

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                tiffToPdf().convert(invalidTiffFile, outputFile)
            }
        }

        // Clean up
        invalidTiffFile.delete()
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
                tiffToPdf().convert(emptyInputStream, outputStream)
            }
        }
    }

    /**
     * Test conversion with corrupted TIFF data
     */
    @Test
    fun `test conversion with corrupted tiff data`() = runBlocking {
        // Create corrupted TIFF data (some random bytes that aren't a valid TIFF)
        val corruptedTiffData = ByteArray(100) { it.toByte() }
        val corruptedInputStream = ByteArrayInputStream(corruptedTiffData)
        val outputStream = ByteArrayOutputStream()

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                tiffToPdf().convert(corruptedInputStream, outputStream)
            }
        }
    }

    /**
     * Test conversion with non-existent input file
     */
    @Test
    fun `test conversion with non-existent input file`() = runBlocking {
        // Non-existent file
        val nonExistentFile = File("non-existent-file.tiff")
        val outputFile = File.createTempFile("output", ".pdf")

        // This should throw an exception
        assertThrows(Exception::class.java) {
            runBlocking {
                tiffToPdf().convert(nonExistentFile, outputFile)
            }
        }

        // Clean up
        outputFile.delete()
    }

    /**
     * Test conversion with invalid PDF version configuration
     */
    @Test
    fun `test conversion with invalid pdf version configuration`() = runBlocking {
        // Check if test TIFF exists
        val testTiffFile = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")
        if (!testTiffFile.exists()) {
            return@runBlocking
        }

        val outputFile = File.createTempFile("output", ".pdf")

        // This should either fail or handle gracefully
        tiffToPdf(
            pdfConfig = {
                pdfVersion = "invalid-version" // Invalid PDF version
            }
        ).convert(testTiffFile, outputFile)

        // If it doesn't throw, verify output was created
        assert(outputFile.exists() && outputFile.length() > 0)

        outputFile.delete()

    }
}
