package dev.waylon.apexflow.conversion

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

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

    private val logger = LoggerFactory.getLogger(TiffToPdfEdgeCaseTest::class.java)

    /**
     * Test conversion with invalid TIFF file
     */
    @Test
    fun `test conversion with invalid tiff file`() = runBlocking {
        logger.info("=== Test: Conversion with invalid TIFF file ===")

        // Create a non-TIFF file
        val invalidTiffFile = File.createTempFile("invalid", ".tiff")
        invalidTiffFile.writeText("This is not a valid TIFF file")

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    tiffToPdf().convert(invalidTiffFile, File.createTempFile("output", ".pdf"))
                }
            }
            logger.info("✓ Conversion with invalid TIFF file correctly threw exception")
        } finally {
            invalidTiffFile.delete()
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
                    tiffToPdf().convert(emptyInputStream, outputStream)
                }
            }
            logger.info("✓ Conversion with empty input stream correctly threw exception")
        } finally {
            emptyInputStream.close()
            outputStream.close()
        }
    }

    /**
     * Test conversion with corrupted TIFF data
     */
    @Test
    fun `test conversion with corrupted tiff data`() = runBlocking {
        logger.info("=== Test: Conversion with corrupted TIFF data ===")

        // Create corrupted TIFF data (some random bytes that aren't a valid TIFF)
        val corruptedTiffData = ByteArray(100) { it.toByte() }
        val corruptedInputStream = ByteArrayInputStream(corruptedTiffData)
        val outputStream = ByteArrayOutputStream()

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    tiffToPdf().convert(corruptedInputStream, outputStream)
                }
            }
            logger.info("✓ Conversion with corrupted TIFF data correctly threw exception")
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
        val nonExistentFile = File("non-existent-file.tiff")
        val outputFile = File.createTempFile("output", ".pdf")

        try {
            // This should throw an exception
            assertThrows(Exception::class.java) {
                runBlocking {
                    tiffToPdf().convert(nonExistentFile, outputFile)
                }
            }
            logger.info("✓ Conversion with non-existent input file correctly threw exception")
        } finally {
            outputFile.delete()
        }
    }

    /**
     * Test conversion with invalid PDF version configuration
     */
    @Test
    fun `test conversion with invalid pdf version configuration`() = runBlocking {
        logger.info("=== Test: Conversion with invalid PDF version configuration ===")

        // Check if test TIFF exists
        val testTiffFile = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")
        if (!testTiffFile.exists()) {
            logger.warn("Test TIFF file not found, skipping test")
            return@runBlocking
        }

        val outputFile = File.createTempFile("output", ".pdf")

        try {
            // This should either fail or handle gracefully
            tiffToPdf(
                pdfConfig = {
                    pdfVersion = "invalid-version" // Invalid PDF version
                }
            ).convert(testTiffFile, outputFile)

            // If it doesn't throw, verify output was created
            assert(outputFile.exists() && outputFile.length() > 0)
            logger.info("✓ Conversion with invalid PDF version configuration handled gracefully")
        } catch (e: Exception) {
            logger.info("✓ Conversion with invalid PDF version configuration correctly threw exception: ${e.message}")
        } finally {
            outputFile.delete()
        }
    }
}
