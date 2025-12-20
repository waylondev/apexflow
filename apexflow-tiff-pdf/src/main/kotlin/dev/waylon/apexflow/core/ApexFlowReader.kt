package dev.waylon.apexflow.core

import java.io.File
import java.io.InputStream

/**
 * ApexFlow reader interface
 *
 * Defines a common interface for all flow-based readers
 * Flow<Unit> -> Flow<O>
 *
 * @param O Output type
 */
interface ApexFlowReader<O> : ApexFlow<Unit, O> {
    /**
     * Create a reader from a file
     */
    fun fromFile(file: File): ApexFlowReader<O>

    /**
     * Create a reader from an input stream
     */
    fun fromInputStream(inputStream: InputStream): ApexFlowReader<O>

    /**
     * Create a reader from a file path
     */
    fun fromPath(filePath: String): ApexFlowReader<O>
}
