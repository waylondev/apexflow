package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.core.dsl.withLogging
import dev.waylon.apexflow.core.dsl.withTiming
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.awt.image.BufferedImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
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

        // Step 1: PDF Reading - As an independent ApexFlow instance
        val pdfReadFlow = apexFlow<Pair<File, File>, Pair<File, Flow<BufferedImage>>> {
            transformOnIO { (pdfFile, tiffFile) ->
                logger.info("Starting PDF reading step")
                logger.info("Processing file: {}", pdfFile.name)

                // Step 1: Read PDF on IO dispatcher (reader handles its own coroutines)
                val imagesFlow = PdfImageReader(pdfFile) {
                    dpi = 100f
                }.read()
                    .flowOn(Dispatchers.IO)

                // Return the pair of tiffFile and imagesFlow
                Pair(tiffFile, imagesFlow)
            }
        }
            .withTiming("PDF Reading Phase Duration")
            .withLogging("PDF Reading Phase Log")

        // Step 2: TIFF Writing - As an independent ApexFlow instance
        val tiffWriteFlow = apexFlow<Pair<File, Flow<BufferedImage>>, Unit> {
            transformOnIO { (tiffFile, imagesFlow) ->
                logger.info("Starting TIFF writing step")
                logger.info("Processing file: {}", tiffFile.name)

                // Step 2: Write TIFF on IO dispatcher (writer handles its own coroutines)
                TiffWriter(tiffFile) {
                    compressionType = "JPEG"
                    compressionQuality = 90f
                }.write(imagesFlow)
            }
        }
            .withTiming("TIFF Writing Phase Duration")
            .withLogging("TIFF Writing Phase Log")

        // Combine both steps and add total duration statistics
        val pdfToTiffFlow = (pdfReadFlow + tiffWriteFlow)
            .withTiming("Total PDF to TIFF Conversion Duration")
            .withLogging("PDF to TIFF Conversion Detailed Log")

        // Execute the flow: pass the input file pair to the flow
        // Use execute method, which is a convenient wrapper around the transform method
        pdfToTiffFlow.execute(inputPdf to outputTiff).toList()

        // Verify that output file was created
        assertTrue(outputTiff.exists(), "Output TIFF file was not created")
        assertTrue(outputTiff.length() > 0, "Output TIFF file is empty")

        logger.info("Conversion completed successfully!")
        logger.info("Output size: {} KB", outputTiff.length() / 1024)
    }
}