package dev.waylon.apexflow.core.plugin

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.plugin.impl.withLogging
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试日志插件的功能
 */
class LoggingPluginTest {
    
    @Test
    fun `test logging plugin with custom logger name`() = runBlocking {
        // 1. 定义数据模型
        data class Input(val value: Int)
        data class Output(val value: Int, val processed: Boolean)
        
        // 2. 定义处理逻辑
        suspend fun processInput(input: Input): Output {
            // 模拟处理延迟
            kotlinx.coroutines.delay(10)
            return Output(input.value * 2, processed = true)
        }
        
        // 3. 创建带有自定义日志名称的流程
        val flow = apexFlow<Input, Output> { 
            map { processInput(it) }
        }.withLogging("test.apexflow") // 使用自定义日志名称
        
        // 4. 执行流程
        val inputs = flow {
            emit(Input(1))
            emit(Input(2))
            emit(Input(3))
        }
        
        val results = flow.transform(inputs).toList()
        
        // 5. 验证结果
        assertEquals(3, results.size)
        assertEquals(Output(2, true), results[0])
        assertEquals(Output(4, true), results[1])
        assertEquals(Output(6, true), results[2])
    }
    
    @Test
    fun `test logging plugin with default logger`() = runBlocking {
        // 1. 定义数据模型
        data class Input(val value: Int)
        data class Output(val value: Int)
        
        // 2. 创建带有默认日志的流程
        val flow = apexFlow<Input, Output> { 
            map { Output(it.value * 3) }
        }.withLogging() // 使用默认日志名称
        
        // 3. 执行流程
        val inputs = flow {
            emit(Input(1))
            emit(Input(2))
        }
        
        val results = flow.transform(inputs).toList()
        
        // 4. 验证结果
        assertEquals(2, results.size)
        assertEquals(Output(3), results[0])
        assertEquals(Output(6), results[1])
    }
}
