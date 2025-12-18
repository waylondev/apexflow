package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.transformOnDefault
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.core.dsl.withLogging
import dev.waylon.apexflow.core.dsl.withTiming
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Test case for converting a specific PDF file to TIFF
 *
 * This test converts the spring-boot-reference-3.3.0-977.pdf file from the build directory to TIFF
 */
class PdfToTiffConversionTest {

    private val logger = LoggerFactory.getLogger(PdfToTiffConversionTest::class.java)

    /**
     * Test converting spring-boot-reference-3.3.0-977.pdf to TIFF
     *
     * This test demonstrates the core ApexFlow design: "Everything is Flow"
     * with multiple transform steps and different coroutine dispatchers.
     *
     * Steps:
     * 1. Read PDF file (IO intensive)
     * 2. Parse PDF to get image flow (CPU intensive)
     * 3. Process images (CPU intensive)
     * 4. Write TIFF file (IO intensive)
     */
    @Test
    fun `test converting spring-boot-reference pdf to tiff`() = runBlocking {
        // Define input and output file paths
        val inputPdf = File("build/spring-boot-reference-406.pdf")
        val outputTiff = File("build/spring-boot-reference-406.tiff")

        // Check if input file exists
        assertTrue(inputPdf.exists(), "Input PDF file does not exist: ${inputPdf.absolutePath}")

        logger.info("Converting PDF to TIFF...")
        logger.info("Input: {}", inputPdf.absolutePath)
        logger.info("Output: {}", outputTiff.absolutePath)
        logger.info("Input size: {} KB", inputPdf.length() / 1024)

        // Core design: Create ApexFlow instance, embodying "Everything is Flow" principle
        val pdfToTiffFlow = apexFlow<Pair<File, File>, Unit> {
            // Step 1: Open file streams (IO intensive, using IO dispatcher)
            transformOnIO { (pdfFile, tiffFile) ->
                logger.debug("Step 1: Opening streams on {}", Thread.currentThread().name)
                val inputStream = Files.newInputStream(pdfFile.toPath())
                val outputStream = Files.newOutputStream(tiffFile.toPath())
                Triple(inputStream, outputStream, tiffFile) // Return triple, pass to next step
            }

                // Step 2: Read PDF to get image flow (CPU intensive, using Default dispatcher)
                .transformOnDefault { (inputStream, outputStream, tiffFile) ->
                    logger.debug("Step 2: Reading PDF on {}", Thread.currentThread().name)
                    val pdfReader = PdfImageReader(inputStream) {
                        dpi = 100f // Set DPI
                    }
                    val imagesFlow = pdfReader.read() // Get image flow
                    Triple(imagesFlow, outputStream, inputStream) // Return triple, pass to next step
                }

                // Step 3: Create TIFF writer and write (IO intensive, using IO dispatcher)
                .transformOnIO { (imagesFlow, outputStream, inputStream) ->
                    logger.debug("Step 3: Writing TIFF on {}", Thread.currentThread().name)
                    try {
                        val tiffWriter = TiffWriter(outputStream) {
                            compressionType = "JPEG" // Set compression type
                            compressionQuality = 90f // Set compression quality
                        }
                        tiffWriter.write(imagesFlow) // Write TIFF file
                    } finally {
                        // Ensure streams are closed
                        inputStream.close()
                        outputStream.close()
                    }
                }
        }.withTiming("PDF to TIFF Conversion time")
            .withLogging("PDF to TIFF Conversion log")

        // Execute the flow: pass the input file pair to the flow
        // Use execute method, which is a convenient wrapper around the transform method
        val result = pdfToTiffFlow.execute(inputPdf to outputTiff).toList()

        // Verify that output file was created
        assertTrue(outputTiff.exists(), "Output TIFF file was not created")
        assertTrue(outputTiff.length() > 0, "Output TIFF file is empty")

        logger.info("Conversion completed successfully!")
        logger.info("Output size: {} KB", outputTiff.length() / 1024)
    }
}