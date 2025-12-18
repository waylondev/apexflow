package dev.waylon.apexflow.core.plugin

import dev.waylon.apexflow.core.ApexFlow

/**
 * Plugin interface for ApexFlow, used to extend ApexFlow functionality
 * Plugins can add additional features like logging, monitoring, retry, etc.
 *
 * This design supports both core plugins provided by the framework and custom plugins implemented by clients:
 * - Core plugins are implemented in the `plugin.impl` package
 * - Clients can implement this interface to create custom plugins
 *
 * @see dev.waylon.apexflow.core.plugin.impl.LoggingPlugin for a core plugin example
 */
interface ApexFlowPlugin {
    /**
     * Wrap the original flow, adding plugin functionality
     * @param flow The original flow to wrap
     * @return A new flow with plugin functionality added
     */
    fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O>
}
