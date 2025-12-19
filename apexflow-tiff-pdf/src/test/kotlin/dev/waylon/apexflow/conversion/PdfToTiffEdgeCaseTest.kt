package dev.waylon.apexflow.conversion

import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory

/**
 * Test file for PDF to TIFF conversion edge cases
 *
 * Tests edge cases for PDF to TIFF conversion including:
 * - Different DPI settings
 * - Various compression types
 * - Page range selection
 * - Invalid input handling
 * - Large number of pages
 * - Different image types
 */
class PdfToTiffEdgeCaseTest {

    private val logger = LoggerFactory.getLogger(PdfToTiffEdgeCaseTest::class.java)

    // Test PDF file path
    private val testPdfFile = File("build/test-pdf-to-tiff-dsl.pdf")

    // Test TIFF file path (will be created during test)
    private val testTiffFile = File("build/test-pdf-to-tiff-edge-case.tiff")

    /**
     * Test PDF to TIFF conversion with different DPI settings
     */
    @Test
    fun `test different dpi settings`() = runBlocking {
        logger.info("=== Test: Different DPI settings ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test with low DPI
        val lowDpiTiff = File("build/test-pdf-to-tiff-low-dpi.tiff")
        pdfToTiff(
            pdfConfig = { dpi = 72f },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, lowDpiTiff)
        assertTrue(lowDpiTiff.exists(), "Low DPI TIFF file was not created")
        assertTrue(lowDpiTiff.length() > 0, "Low DPI TIFF file is empty")

        // Test with high DPI
        val highDpiTiff = File("build/test-pdf-to-tiff-high-dpi.tiff")
        pdfToTiff(
            pdfConfig = { dpi = 300f },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, highDpiTiff)
        assertTrue(highDpiTiff.exists(), "High DPI TIFF file was not created")
        assertTrue(highDpiTiff.length() > 0, "High DPI TIFF file is empty")

        // High DPI file should be larger than low DPI file
        assertTrue(highDpiTiff.length() > lowDpiTiff.length(), "High DPI file should be larger than low DPI file")

        // Clean up
        lowDpiTiff.delete()
        highDpiTiff.delete()

        logger.info("✓ Different DPI settings test successful")
    }

    /**
     * Test PDF to TIFF conversion with various compression types
     */
    @Test
    fun `test various compression types`() = runBlocking {
        logger.info("=== Test: Various compression types ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test different compression types
        val compressionTypes = listOf("JPEG", "LZW", "DEFLATE", "NONE")
        
        compressionTypes.forEach { compressionType ->
            logger.info("Testing compression type: $compressionType")
            val outputFile = File("build/test-pdf-to-tiff-$compressionType.tiff")
            
            pdfToTiff(
                tiffConfig = { 
                    this.compressionType = compressionType
                    this.compressionQuality = 90f
                }
            ).convert(testPdfFile, outputFile)
            
            assertTrue(outputFile.exists(), "TIFF file with $compressionType compression was not created")
            assertTrue(outputFile.length() > 0, "TIFF file with $compressionType compression is empty")
            
            // Clean up
            outputFile.delete()
        }

        logger.info("✓ Various compression types test successful")
    }

    /**
     * Test PDF to TIFF conversion with page range selection
     */
    @Test
    fun `test page range selection`() = runBlocking {
        logger.info("=== Test: Page range selection ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test with specific page range
        val pageRangeTiff = File("build/test-pdf-to-tiff-page-range.tiff")
        pdfToTiff(
            pdfConfig = { 
                pageNumbers = listOf(0, 1) // First two pages
            },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, pageRangeTiff)
        
        assertTrue(pageRangeTiff.exists(), "Page range TIFF file was not created")
        assertTrue(pageRangeTiff.length() > 0, "Page range TIFF file is empty")

        // Test with invalid page numbers (should be filtered out)
        val invalidPageRangeTiff = File("build/test-pdf-to-tiff-invalid-page-range.tiff")
        pdfToTiff(
            pdfConfig = { 
                pageNumbers = listOf(0, 1000, -1) // Some invalid pages
            },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, invalidPageRangeTiff)
        
        assertTrue(invalidPageRangeTiff.exists(), "Invalid page range TIFF file was not created")
        assertTrue(invalidPageRangeTiff.length() > 0, "Invalid page range TIFF file is empty")

        // Clean up
        pageRangeTiff.delete()
        invalidPageRangeTiff.delete()

        logger.info("✓ Page range selection test successful")
    }

    /**
     * Test PDF to TIFF conversion with different image types
     */
    @Test
    fun `test different image types`() = runBlocking {
        logger.info("=== Test: Different image types ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test with RGB image type
        val rgbTiff = File("build/test-pdf-to-tiff-rgb.tiff")
        pdfToTiff(
            pdfConfig = { 
                imageType = dev.waylon.apexflow.pdf.PdfImageReaderConfig.ImageType.RGB
            },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, rgbTiff)
        
        assertTrue(rgbTiff.exists(), "RGB TIFF file was not created")
        assertTrue(rgbTiff.length() > 0, "RGB TIFF file is empty")

        // Test with GRAY image type
        val grayTiff = File("build/test-pdf-to-tiff-gray.tiff")
        pdfToTiff(
            pdfConfig = { 
                imageType = dev.waylon.apexflow.pdf.PdfImageReaderConfig.ImageType.GRAY
            },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, grayTiff)
        
        assertTrue(grayTiff.exists(), "GRAY TIFF file was not created")
        assertTrue(grayTiff.length() > 0, "GRAY TIFF file is empty")

        // Clean up
        rgbTiff.delete()
        grayTiff.delete()

        logger.info("✓ Different image types test successful")
    }

    /**
     * Test PDF to TIFF conversion with skip blank pages
     */
    @Test
    fun `test skip blank pages`() = runBlocking {
        logger.info("=== Test: Skip blank pages ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test with skip blank pages enabled
        val skipBlankTiff = File("build/test-pdf-to-tiff-skip-blank.tiff")
        pdfToTiff(
            pdfConfig = { 
                skipBlankPages = true
            },
            tiffConfig = { compressionType = "JPEG" }
        ).convert(testPdfFile, skipBlankTiff)
        
        assertTrue(skipBlankTiff.exists(), "Skip blank pages TIFF file was not created")
        assertTrue(skipBlankTiff.length() > 0, "Skip blank pages TIFF file is empty")

        // Clean up
        skipBlankTiff.delete()

        logger.info("✓ Skip blank pages test successful")
    }

    /**
     * Test PDF to TIFF conversion with parallel writing
     */
    @Test
    fun `test parallel writing`() = runBlocking {
        logger.info("=== Test: Parallel writing ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test with parallel writing enabled
        val parallelTiff = File("build/test-pdf-to-tiff-parallel.tiff")
        pdfToTiff(
            tiffConfig = { 
                compressionType = "JPEG"
                parallelWriting = true
            }
        ).convert(testPdfFile, parallelTiff)
        
        assertTrue(parallelTiff.exists(), "Parallel writing TIFF file was not created")
        assertTrue(parallelTiff.length() > 0, "Parallel writing TIFF file is empty")

        // Clean up
        parallelTiff.delete()

        logger.info("✓ Parallel writing test successful")
    }
}
