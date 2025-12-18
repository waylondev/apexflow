package dev.waylon.apexflow.core.dsl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test

class DispatcherVerificationTest {

    @Test
    fun `verify transformOn switches dispatchers correctly`() = runBlocking {
        println("=== Testing transformOn dispatcher switching ===")
        println("Main thread: ${Thread.currentThread().name}")

        // Test with different dispatchers
        flow {
            println("Flow emitting on: ${Thread.currentThread().name}")
            emit(1)
        }
            .transformOnIO {
                println("TransformOn IO running on: ${Thread.currentThread().name}")
                it * 2
            }
            .transformOnDefault {
                println("TransformOn Default running on: ${Thread.currentThread().name}")
                it + 1
            }
            .transformOnIO {
                println("TransformOn IO again running on: ${Thread.currentThread().name}")
                "Result: $it"
            }
            .collect {
                println("Collecting on: ${Thread.currentThread().name}")
                println("Final result: $it")
            }

        println("=== Test completed ===")
    }

    @Test
    fun `verify withContext switches dispatchers`() = runBlocking {
        println("\n=== Testing withContext dispatcher switching ===")
        println("Main thread: ${Thread.currentThread().name}")

        val result1 = withContext(Dispatchers.IO) {
            println("withContext IO running on: ${Thread.currentThread().name}")
            1 * 2
        }

        val result2 = withContext(Dispatchers.Default) {
            println("withContext Default running on: ${Thread.currentThread().name}")
            result1 + 1
        }

        println("Final result: $result2")
        println("=== Test completed ===")
    }
}
