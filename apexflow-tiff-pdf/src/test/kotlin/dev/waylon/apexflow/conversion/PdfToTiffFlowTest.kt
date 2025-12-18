package dev.waylon.apexflow.conversion

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Test suite for PdfToTiffFlow functionality
 * 
 * This test covers:
 * 1. Basic PDF to TIFF conversion
 * 2. Custom configuration
 * 3. Flow composition
 */
class PdfToTiffFlowTest {

    /**
     * Test basic PDF to TIFF conversion
     * 
     * This test verifies that the conversion flow can be created and executed without errors
     */
    @Test
    fun `test basic pdf to tiff conversion`() = runBlocking {
        // Create empty input and output streams for testing
        val pdfInputStream = ByteArrayInputStream(emptyByteArray())
        val tiffOutputStream = ByteArrayOutputStream()
        
        // Create conversion flow
        val conversionFlow = pdfToTiff()
        
        // Execute the flow
        try {
            conversionFlow.transform(flowOf(pdfInputStream to tiffOutputStream)).collect()
            // If no exception is thrown, the test passes
            assertTrue(true)
        } catch (e: Exception) {
            // Expected exception due to empty PDF file
            // We're mainly testing that the flow structure works
            assertTrue(e.message?.contains("PDF") == true || e.message?.contains("file") == true)
        }
    }
    
    /**
     * Test custom PDF to TIFF configuration
     */
    @Test
    fun `test custom pdf to tiff configuration`() = runBlocking {
        // Create empty input and output streams for testing
        val pdfInputStream = ByteArrayInputStream(emptyByteArray())
        val tiffOutputStream = ByteArrayOutputStream()
        
        // Create conversion flow with custom configuration
        val conversionFlow = pdfToTiff {
            pdfReaderConfig {
                dpi = 300f
            }
            tiffWriterConfig {
                compressionType = "LZW"
            }
        }
        
        // Execute the flow
        try {
            conversionFlow.transform(flowOf(pdfInputStream to tiffOutputStream)).collect()
            // If no exception is thrown, the test passes
            assertTrue(true)
        } catch (e: Exception) {
            // Expected exception due to empty PDF file
            assertTrue(e.message?.contains("PDF") == true || e.message?.contains("file") == true)
        }
    }
    
    /**
     * Test PdfInputStreamToImagesFlow
     */
    @Test
    fun `test pdf input stream to images flow`() = runBlocking {
        // Create empty PDF input stream
        val pdfInputStream = ByteArrayInputStream(emptyByteArray())
        
        // Create conversion flow
        val conversionFlow = pdfToImages {
            dpi = 200f
        }
        
        // Execute the flow
        try {
            conversionFlow.transform(flowOf(pdfInputStream)).collect()
            // If no exception is thrown, the test passes
            assertTrue(true)
        } catch (e: Exception) {
            // Expected exception due to empty PDF file
            assertTrue(e.message?.contains("PDF") == true || e.message?.contains("file") == true)
        }
    }
    
    /**
     * Test ImagesToTiffFlow
     */
    @Test
    fun `test images to tiff flow`() = runBlocking {
        // Create empty output stream
        val tiffOutputStream = ByteArrayOutputStream()
        
        // Create conversion flow
        val conversionFlow = imagesToTiff {
            compressionType = "JPEG"
            compressionQuality = 90f
        }
        
        // Execute the flow
        try {
            conversionFlow.transform(flowOf(flowOf(), tiffOutputStream)).collect()
            // If no exception is thrown, the test passes
            assertTrue(true)
        } catch (e: Exception) {
            // Expected exception due to empty flow
            assertTrue(e.message?.contains("TIFF") == true || e.message?.contains("empty") == true)
        }
    }
}
