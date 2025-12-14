package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for WorkflowConfig
 */
class WorkflowConfigTest {

    /**
     * Test default configuration values
     */
    @Test
    fun testDefaultConfiguration() {
        // Create workflow config with default values
        val config = WorkflowConfig()

        // Verify default values
        assertEquals(100, config.readBufferSize)
        assertEquals(100, config.processBufferSize)
        assertEquals(Dispatchers.IO, config.readDispatcher)
        assertEquals(Dispatchers.Default, config.processDispatcher)
        assertEquals(Dispatchers.IO, config.writeDispatcher)
    }

    /**
     * Test custom configuration values
     */
    @Test
    fun testCustomConfiguration() {
        // Create workflow config with custom values
        val customErrorHandler = { e: Throwable -> println("Custom error: ${e.message}") }
        val config = WorkflowConfig(
            readBufferSize = 200,
            processBufferSize = 200,
            errorHandler = customErrorHandler,
            readDispatcher = Dispatchers.Default,
            processDispatcher = Dispatchers.IO,
            writeDispatcher = Dispatchers.Default
        )

        // Verify custom values
        assertEquals(200, config.readBufferSize)
        assertEquals(200, config.processBufferSize)
        assertEquals(Dispatchers.Default, config.readDispatcher)
        assertEquals(Dispatchers.IO, config.processDispatcher)
        assertEquals(Dispatchers.Default, config.writeDispatcher)
    }

    /**
     * Test copy functionality
     */
    @Test
    fun testCopyConfiguration() {
        // Create original config
        val originalConfig = WorkflowConfig(readBufferSize = 300, processBufferSize = 300)

        // Copy config
        val copiedConfig = originalConfig.copy(readBufferSize = 400)

        // Verify original config unchanged
        assertEquals(300, originalConfig.readBufferSize)
        assertEquals(300, originalConfig.processBufferSize)

        // Verify copied config has new values
        assertEquals(400, copiedConfig.readBufferSize)
        assertEquals(300, copiedConfig.processBufferSize)
    }
}
