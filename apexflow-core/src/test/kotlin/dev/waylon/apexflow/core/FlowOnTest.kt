package dev.waylon.apexflow.core

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.core.dsl.transformOnDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.system.measureTimeMillis

/**
 * 测试如何在每个节点指定运行的协程调度器
 * 实现生产消费分离，提高性能
 */
class FlowOnTest {
    
    @Test
    fun `test flow with different coroutine dispatchers`() = runBlocking {
        // 1. 定义数据模型
        data class Product(val id: String, val name: String, val price: Double)
        data class ProcessedProduct(val id: String, val name: String, val price: Double, val discountedPrice: Double)
        
        // 2. 定义不同类型的处理逻辑
        
        // IO 密集型操作：模拟从数据库读取数据
        suspend fun fetchProductDetails(id: String): Product {
            // 模拟 IO 延迟
            kotlinx.coroutines.delay(50)
            return Product(id, "Product $id", 100.0)
        }
        
        // CPU 密集型操作：模拟复杂计算
        suspend fun calculateDiscount(product: Product): ProcessedProduct {
            // 模拟 CPU 密集型计算
            kotlinx.coroutines.delay(25) // 模拟计算延迟
            val discount = product.price * 0.9 // 10% 折扣
            return ProcessedProduct(product.id, product.name, product.price, discount)
        }
        
        // 3. 创建流程 - 为每个节点指定不同的协程调度器
        val productFlow = apexFlow<String, ProcessedProduct> {
            // 生产阶段：IO 密集型操作，使用 IO 调度器
            transformOnIO { productId ->
                fetchProductDetails(productId)
            }
                // 消费阶段：CPU 密集型操作，使用 Default 调度器
                .transformOnDefault { product ->
                    calculateDiscount(product)
                }
        }
        
        // 4. 执行流程 - 生产消费分离
        val productIds = flow {
            for (i in 1..5) {
                emit("product_$i")
            }
        }.flowOn(Dispatchers.IO) // 生产端也可以指定调度器
        
        val results = productFlow.transform(productIds).toList()
        
        // 验证结果
        assertEquals(5, results.size)
        results.forEach {
            assertEquals(90.0, it.discountedPrice)
        }
    }
    
    @Test
    fun `test performance improvement with dispatcher separation`() = runBlocking {
        // 1. 定义数据模型
        data class Input(val value: Int)
        data class Output(val value: Int, val processed: Boolean)
        
        // 2. 定义处理逻辑
        suspend fun ioIntensiveOperation(input: Input): Input {
            kotlinx.coroutines.delay(30)
            return input
        }
        
        suspend fun cpuIntensiveOperation(input: Input): Output {
            kotlinx.coroutines.delay(15)
            return Output(input.value, processed = true)
        }
        
        // 3. 创建流程 - 策略 1：默认调度器
        val defaultFlow = apexFlow<Input, Output> {
            map { ioIntensiveOperation(it) }
                .map { cpuIntensiveOperation(it) }
        }
        
        // 4. 创建流程 - 策略 2：生产消费分离
        val optimizedFlow = apexFlow<Input, Output> {
            transformOnIO { ioIntensiveOperation(it) }
                .transformOnDefault { cpuIntensiveOperation(it) }
        }
        
        // 5. 执行流程并测量性能
        val inputs = flow {
            for (i in 1..10) {
                emit(Input(i))
            }
        }
        
        // 测量默认策略的执行时间
        val defaultTime = measureTimeMillis {
            defaultFlow.transform(inputs).toList()
        }
        
        // 测量优化策略的执行时间
        val optimizedTime = measureTimeMillis {
            optimizedFlow.transform(inputs).toList()
        }
        
        println("Default strategy time: $defaultTime ms")
        println("Optimized strategy time: $optimizedTime ms")
        println("Performance improvement: ${((defaultTime - optimizedTime) / defaultTime.toDouble() * 100).toInt()}%")
        
        // 优化策略应该更快
        assert(optimizedTime < defaultTime) {
            "Optimized strategy should be faster than default strategy"
        }
    }
}
