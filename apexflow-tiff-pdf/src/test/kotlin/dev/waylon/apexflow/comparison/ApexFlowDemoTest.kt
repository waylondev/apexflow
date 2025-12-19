package dev.waylon.apexflow.comparison

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.apexFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * ApexFlow 简单演示测试
 */
class ApexFlowDemoTest {

    /**
     * 演示基本的ApexFlow创建和执行
     */
    @Test
    fun `demo basic apexflow`() = runBlocking {
        // 创建一个简单的ApexFlow，将字符串转换为大写
        val upperCaseFlow: ApexFlow<String, String> = apexFlow {
            // 直接使用Flow API进行转换
            transform { input ->
                emit(input.uppercase())
            }
        }
        
        // 测试数据
        val input = "hello apexflow"
        
        // 执行Flow转换
        val resultFlow = upperCaseFlow.transform(flow { emit(input) })
        
        // 收集结果
        val results = mutableListOf<String>()
        resultFlow.collect {
            results.add(it)
        }
        
        // 验证结果
        assertEquals(1, results.size)
        assertEquals("HELLO APEXFLOW", results[0])
        
        println("✓ 基本ApexFlow演示成功!")
        println("   输入: $input")
        println("   输出: ${results[0]}")
    }
    
    /**
     * 演示ApexFlow的组合能力
     */
    @Test
    fun `demo apexflow composition`() = runBlocking {
        // 创建第一个Flow: 字符串长度
        val lengthFlow: ApexFlow<String, Int> = apexFlow {
            transform { input ->
                emit(input.length)
            }
        }
        
        // 创建第二个Flow: 长度翻倍
        val doubleFlow: ApexFlow<Int, Int> = apexFlow {
            transform { input ->
                emit(input * 2)
            }
        }
        
        // 组合两个Flow
        val composedFlow: ApexFlow<String, Int> = lengthFlow + doubleFlow
        
        // 测试数据
        val input = "apexflow"
        
        // 执行组合Flow
        val resultFlow = composedFlow.transform(flow { emit(input) })
        
        // 收集结果
        val results = mutableListOf<Int>()
        resultFlow.collect {
            results.add(it)
        }
        
        // 验证结果: "apexflow"长度是8，翻倍后是16
        assertEquals(1, results.size)
        assertEquals(16, results[0])
        
        println("✓ ApexFlow组合演示成功!")
        println("   输入: $input")
        println("   长度: ${input.length}")
        println("   长度翻倍: ${results[0]}")
    }
    
    /**
     * 演示多输入处理
     */
    @Test
    fun `demo multiple inputs`() = runBlocking {
        // 创建Flow: 数字加10
        val addTenFlow: ApexFlow<Int, Int> = apexFlow {
            transform { input ->
                emit(input + 10)
            }
        }
        
        // 测试数据
        val inputs = listOf(1, 2, 3, 4, 5)
        
        // 创建输入Flow
        val inputFlow = flow {
            inputs.forEach { emit(it) }
        }
        
        // 执行Flow转换
        val resultFlow = addTenFlow.transform(inputFlow)
        
        // 收集结果
        val results = mutableListOf<Int>()
        resultFlow.collect {
            results.add(it)
        }
        
        // 验证结果
        assertEquals(inputs.size, results.size)
        inputs.forEachIndexed { index, input ->
            assertEquals(input + 10, results[index])
        }
        
        println("✓ 多输入处理演示成功!")
        println("   输入: $inputs")
        println("   输出: $results")
    }
    
    /**
     * 演示ApexFlow的核心优势
     */
    @Test
    fun `demo apexflow advantages`() {
        println("\n=== ApexFlow 核心优势 ===")
        println("1. 声明式编程")
        println("   - 清晰的业务流程定义")
        println("   - 易于理解和维护")
        println("   - 减少样板代码")
        
        println("\n2. 基于Flow")
        println("   - 充分利用Kotlin Flow的异步能力")
        println("   - 支持背压处理")
        println("   - 无缝集成Kotlin协程")
        
        println("\n3. 组合能力")
        println("   - 支持模块化设计")
        println("   - 流程可复用")
        println("   - 易于扩展")
        
        println("\n4. 类型安全")
        println("   - 编译时类型检查")
        println("   - 清晰的输入输出类型")
        
        println("\n5. 异步处理")
        println("   - 内置异步支持")
        println("   - 高效利用系统资源")
    }
}