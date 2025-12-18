package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.transformOnDefault
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Test case for converting a specific PDF file to TIFF
 *
 * This test converts the spring-boot-reference-3.3.0-977.pdf file from the build directory to TIFF
 */
class PdfToTiffConversionTest {

    /**
     * Test converting spring-boot-reference-3.3.0-977.pdf to TIFF
     *
     * This test verifies that we can convert a real PDF file to TIFF format
     */
    @Test
    fun `test converting spring-boot-reference pdf to tiff`() = runBlocking {
        // Define input and output file paths
        val inputPdf = File("build/spring-boot-reference-406.pdf")
        val outputTiff = File("build/spring-boot-reference-406.tiff")

        // Check if input file exists
        assertTrue(inputPdf.exists(), "Input PDF file does not exist: ${inputPdf.absolutePath}")

        println("Converting PDF to TIFF...")
        println("Input: ${inputPdf.absolutePath}")
        println("Output: ${outputTiff.absolutePath}")
        println("Input size: ${inputPdf.length() / 1024} KB")

        // Execute the conversion
        val inputStream = Files.newInputStream(inputPdf.toPath())
        val outputStream = Files.newOutputStream(outputTiff.toPath())
        inputStream.use {
            outputStream.use {
                val pdfToTiffFlow = apexFlow<BufferedImage, BufferedImage> {
                    transformOnIO { image ->
                        image
                    }
                    transformOnDefault { it }
                    transformOnIO { it }
                }
                val pdfImageReader = PdfImageReader(inputStream)
                val tiffWriter = TiffWriter(outputStream)

                val imagesFlow = pdfToTiffFlow.transform(pdfImageReader.read())
                tiffWriter.write(imagesFlow)


                // Verify that output file was created
                assertTrue(outputTiff.exists(), "Output TIFF file was not created")
                assertTrue(outputTiff.length() > 0, "Output TIFF file is empty")

                println("Conversion completed successfully!")
                println("Output size: ${outputTiff.length() / 1024} KB")
            }
        }
    }
}