package dev.waylon.apexflow.core

import dev.waylon.apexflow.core.dsl.apexFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ApexFlow 核心功能测试
 */
class ApexFlowTest {
    
    @Test
    fun `test basic flow transformation`() = runBlocking {
        // 定义数据模型
        data class Input(val value: Int)
        data class Output(val result: String)
        
        // 定义转换逻辑
        suspend fun process(input: Input): Output {
            return Output("Processed: ${input.value}")
        }
        
        // 创建流程 - 使用现代 Kotlin Flow API
        val flow = apexFlow<Input, Output> {
            map { process(it) }
        }
        
        // 执行流程
        val inputs = flow {
            emit(Input(1))
            emit(Input(2))
            emit(Input(3))
        }
        
        val outputs = flow.transform(inputs).toList()
        
        // 验证结果
        assertEquals(3, outputs.size)
        assertEquals("Processed: 1", outputs[0].result)
        assertEquals("Processed: 2", outputs[1].result)
        assertEquals("Processed: 3", outputs[2].result)
    }
    
    @Test
    fun `test multiple transformations`() = runBlocking {
        // 创建流程，包含多个转换步骤 - 利用 Flow 本身的链式调用
        val flow = apexFlow<Int, String> {
            map { it * 2 }
                .map { "Result: $it" }
        }
        
        // 执行流程
        val inputs = flow {
            emit(1)
            emit(2)
            emit(3)
        }
        
        val outputs = flow.transform(inputs).toList()
        
        // 验证结果
        assertEquals(3, outputs.size)
        assertEquals("Result: 2", outputs[0])
        assertEquals("Result: 4", outputs[1])
        assertEquals("Result: 6", outputs[2])
    }
}
