package dev.waylon.apexflow.conversion

/**
 * Base exception class for all conversion-related exceptions
 *
 * @param message Error message
 * @param cause Optional root cause
 */
open class ConversionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when there's an error reading input data
 *
 * @param message Error message
 * @param cause Optional root cause
 */
class ConversionReadException(message: String, cause: Throwable? = null) : ConversionException(message, cause)

/**
 * Exception thrown when there's an error writing output data
 *
 * @param message Error message
 * @param cause Optional root cause
 */
class ConversionWriteException(message: String, cause: Throwable? = null) : ConversionException(message, cause)

/**
 * Exception thrown when there's an error in conversion configuration
 *
 * @param message Error message
 * @param cause Optional root cause
 */
class ConversionConfigException(message: String, cause: Throwable? = null) : ConversionException(message, cause)

/**
 * Exception thrown when the conversion format is not supported
 *
 * @param message Error message
 * @param cause Optional root cause
 */
class ConversionFormatException(message: String, cause: Throwable? = null) : ConversionException(message, cause)
