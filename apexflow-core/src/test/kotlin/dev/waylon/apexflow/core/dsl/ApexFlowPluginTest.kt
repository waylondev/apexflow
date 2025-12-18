package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for ApexFlow plugin functionality
 * 
 * This test covers:
 * - Plugin system
 * - Plugin wrapping
 * - Plugin composition
 */
class ApexFlowPluginTest {

    /**
     * Test basic plugin functionality
     */
    @Test
    fun `test basic plugin functionality`() = runBlocking {
        // Create a simple plugin that adds a prefix to values
        class PrefixPlugin(private val prefix: String) : ApexFlowPlugin {
            override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
                return object : ApexFlow<I, O> {
                    override fun transform(input: Flow<I>): Flow<O> {
                        @Suppress("UNCHECKED_CAST")
                        return flow.transform(input).map { "$prefix$it" } as Flow<O>
                    }
                }
            }
        }

        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        val pluginFlow = workflow.withPlugin(PrefixPlugin("Prefix: "))
        val result = pluginFlow.transform(flowOf(42)).toList()
        assertEquals(listOf("Prefix: 42"), result)
    }

    /**
     * Test multiple plugins
     */
    @Test
    fun `test multiple plugins`() = runBlocking {
        // Plugin 1: Adds prefix
        class PrefixPlugin(private val prefix: String) : ApexFlowPlugin {
            override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
                return object : ApexFlow<I, O> {
                    override fun transform(input: Flow<I>): Flow<O> {
                        @Suppress("UNCHECKED_CAST")
                        return flow.transform(input).map { "$prefix$it" } as Flow<O>
                    }
                }
            }
        }

        // Plugin 2: Adds suffix
        class SuffixPlugin(private val suffix: String) : ApexFlowPlugin {
            override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
                return object : ApexFlow<I, O> {
                    override fun transform(input: Flow<I>): Flow<O> {
                        @Suppress("UNCHECKED_CAST")
                        return flow.transform(input).map { "$it$suffix" } as Flow<O>
                    }
                }
            }
        }

        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        val enhancedFlow = workflow
            .withPlugin(PrefixPlugin("Prefix: "))
            .withPlugin(SuffixPlugin(" :Suffix"))
        
        val result = enhancedFlow.transform(flowOf(42)).toList()
        assertEquals(listOf("Prefix: 42 :Suffix"), result)
    }
}
