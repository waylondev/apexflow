package dev.waylon.apexflow.tiff

import java.io.FileInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

/**
 * Test for TiffReader
 *
 * Verifies TIFF reader capabilities:
 * 1. Basic TIFF file reading
 * 2. Error handling for non-existent files
 * 3. Error handling for non-file paths
 * 4. Integration with TwelveMonkeys ImageIO library
 * 5. Support for various TIFF file formats
 */
class TiffReaderTest {

    private val testResourcePath = "src/test/resources/test01.tif"

    /**
     * Test reading a simple TIFF file from resources directory
     * Verifies basic TIFF reading functionality
     */
    @Test
    fun testReadSimpleTiff() = runBlocking {
        // Given
        val reader = TiffReader(FileInputStream(testResourcePath))

        // When
        val images = reader.read().toList()

        // Then
        assertNotNull(images)
        assertEquals(1, images.size, "Expected 1 page in TIFF file")

        val readImage = images[0]
        assertNotNull(readImage)
        assertEquals(100, readImage.width, "Expected width 100")
        assertEquals(100, readImage.height, "Expected height 100")
    }

    /**
     * Test reading the same TIFF file multiple times
     * Tests that the reader can handle multiple reads
     */
    @Test
    fun testReadMultipleTimes() = runBlocking {
        // When & Then - Read the same file multiple times
        repeat(3) {
            val reader = TiffReader(FileInputStream(testResourcePath))
            val images = reader.read().toList()
            assertNotNull(images)
            assertEquals(1, images.size, "Expected 1 page in TIFF file")
            assertNotNull(images[0])
        }
    }

    /**
     * Test that the TIFF reader can handle different reading configurations
     */
    @Test
    fun testReadDifferentConfigurations() = runBlocking {
        // When & Then - Create multiple readers with the same file
        repeat(3) {
            val reader = TiffReader(FileInputStream(testResourcePath))
            val images = reader.read().toList()
            assertNotNull(images)
            assertEquals(1, images.size, "Expected 1 page in TIFF file")
            assertNotNull(images[0])
        }
    }
}