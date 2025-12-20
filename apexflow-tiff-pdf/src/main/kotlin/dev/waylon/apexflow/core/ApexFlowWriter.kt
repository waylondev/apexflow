package dev.waylon.apexflow.core

import java.io.File
import java.io.OutputStream

/**
 * ApexFlow writer interface
 *
 * Defines a common interface for all flow-based writers
 * Flow<I> -> Flow<Unit>
 *
 * @param I Input type
 */
interface ApexFlowWriter<I> : ApexFlow<I, Unit> {
    /**
     * Create a writer to a file
     */
    fun toFile(file: File): ApexFlowWriter<I>

    /**
     * Create a writer to an output stream
     */
    fun toOutputStream(outputStream: OutputStream): ApexFlowWriter<I>

    /**
     * Create a writer to a file path
     */
    fun toPath(filePath: String): ApexFlowWriter<I>
}
