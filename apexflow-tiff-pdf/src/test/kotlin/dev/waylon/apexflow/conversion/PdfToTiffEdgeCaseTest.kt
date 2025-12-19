package dev.waylon.apexflow.conversion

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

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

    private val logger = LoggerFactory.getLogger(PdfToTiffEdgeCaseTest::class.java)

    // Small test PDF file
    private val smallTestPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")

    /**
     * Test conversion with invalid PDF file
     */
    @Test
    fun `test conversion with invalid pdf file`() = runBlocking {
        logger.info("=== Test: Conversion with invalid PDF file ===")

        // Create a non-PDF file
        val invalidPdfFile = File.createTempFile("invalid", ".pdf")
        invalidPdfFile.writeText("This is not a valid PDF file")

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    pdfToTiff().convert(invalidPdfFile, File.createTempFile("output", ".tiff"))
                }
            }
            logger.info("✓ Conversion with invalid PDF file correctly threw exception")
        } finally {
            invalidPdfFile.delete()
        }
    }

    /**
     * Test conversion with empty input stream
     */
    @Test
    fun `test conversion with empty input stream`() = runBlocking {
        logger.info("=== Test: Conversion with empty input stream ===")

        // Create empty input stream
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))
        val outputStream = ByteArrayOutputStream()

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    pdfToTiff().convert(emptyInputStream, outputStream)
                }
            }
            logger.info("✓ Conversion with empty input stream correctly threw exception")
        } finally {
            emptyInputStream.close()
            outputStream.close()
        }
    }

    /**
     * Test conversion with corrupted PDF data
     */
    @Test
    fun `test conversion with corrupted pdf data`() = runBlocking {
        logger.info("=== Test: Conversion with corrupted PDF data ===")

        // Create corrupted PDF data (valid PDF header but corrupted content)
        val corruptedPdfData = ("%PDF-1.7\n" + "".padStart(100, 'x') + "\n%%EOF").toByteArray()
        val corruptedInputStream = ByteArrayInputStream(corruptedPdfData)
        val outputStream = ByteArrayOutputStream()

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    pdfToTiff().convert(corruptedInputStream, outputStream)
                }
            }
            logger.info("✓ Conversion with corrupted PDF data correctly threw exception")
        } finally {
            corruptedInputStream.close()
            outputStream.close()
        }
    }

    /**
     * Test conversion with non-existent input file
     */
    @Test
    fun `test conversion with non-existent input file`() = runBlocking {
        logger.info("=== Test: Conversion with non-existent input file ===")

        // Non-existent file
        val nonExistentFile = File("non-existent-file.pdf")
        val outputFile = File.createTempFile("output", ".tiff")

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    pdfToTiff().convert(nonExistentFile, outputFile)
                }
            }
            logger.info("✓ Conversion with non-existent input file correctly threw exception")
        } finally {
            outputFile.delete()
        }
    }

    /**
     * Test conversion with invalid DPI configuration
     */
    @Test
    fun `test conversion with invalid dpi configuration`() = runBlocking {
        logger.info("=== Test: Conversion with invalid DPI configuration ===")

        // Check if test PDF exists
        if (!smallTestPdfFile.exists()) {
            logger.warn("Small test PDF not found, skipping test")
            return@runBlocking
        }

        val outputFile = File.createTempFile("output", ".tiff")

        try {
            // This should either fail or handle gracefully with minimum DPI
            pdfToTiff(
                pdfConfig = {
                    dpi = 0f // Invalid DPI
                }
            ).convert(smallTestPdfFile, outputFile)

            // If it doesn't throw, verify output was created
            assert(outputFile.exists() && outputFile.length() > 0)
            logger.info("✓ Conversion with invalid DPI configuration handled gracefully")
        } catch (e: Exception) {
            logger.info("✓ Conversion with invalid DPI configuration correctly threw exception: ${e.message}")
        } finally {
            outputFile.delete()
        }
    }
}
