package dev.waylon.apexflow.core.node

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.node.impl.CacheFlowNode
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试 CacheFlowNode 的功能
 */
class CacheFlowNodeTest {
    
    @Test
    fun `test cache node caches results`() = runBlocking {
        // 1. 定义数据模型
        data class ProductId(val id: String)
        data class Product(val id: String, val name: String, val price: Double)
        
        // 2. 创建一个带有计数器的操作（模拟数据库查询）
        var queryCount = 0
        suspend fun fetchProductFromDatabase(productId: ProductId): Product {
            queryCount++
            // 模拟昂贵的数据库查询操作
            kotlinx.coroutines.delay(50)
            return Product(productId.id, "Product ${productId.id}", 100.0)
        }
        
        // 3. 创建 CacheFlowNode 实例
        val productCacheNode = CacheFlowNode<ProductId, Product>(
            transformer = ::fetchProductFromDatabase
        )
        
        // 4. 集成到 ApexFlow 流程中
        val productFlow = apexFlow<ProductId, Product> { 
            let { productCacheNode.transform(it) }
        }
        
        // 5. 执行流程 - 第一次执行，会加载数据
        val productIds1 = flow {
            emit(ProductId("1"))
            emit(ProductId("2"))
        }
        val results1 = productFlow.transform(productIds1).toList()
        
        // 6. 验证结果和查询次数
        assertEquals(2, results1.size)
        assertEquals(2, queryCount) // 第一次执行，应该查询2次
        
        // 7. 再次执行相同的流程 - 应该从缓存中获取数据
        val productIds2 = flow {
            emit(ProductId("1")) // 重复请求，应该从缓存获取
            emit(ProductId("2")) // 重复请求，应该从缓存获取
            emit(ProductId("3")) // 新请求，应该从 DB 获取
        }
        val results2 = productFlow.transform(productIds2).toList()
        
        // 8. 验证结果和查询次数
        assertEquals(3, results2.size)
        assertEquals(3, queryCount) // 第二次执行，应该只查询1次新数据
        
        // 9. 清除缓存并再次执行
        productCacheNode.clearCache()
        val results3 = productFlow.transform(productIds1).toList()
        
        // 10. 验证结果和查询次数
        assertEquals(2, results3.size)
        assertEquals(5, queryCount) // 清除缓存后，应该重新查询2次
        
        // 11. 验证缓存大小
        assertEquals(2, productCacheNode.cacheSize())
    }
    
    @Test
    fun `test cache node clear functionality`() = runBlocking {
        // 1. 定义简单数据模型
        data class SimpleInput(val id: Int)
        data class SimpleOutput(val result: String)
        
        // 2. 创建转换函数
        suspend fun transform(input: SimpleInput): SimpleOutput {
            return SimpleOutput("Processed: ${input.id}")
        }
        
        // 3. 创建 CacheFlowNode 实例
        val cacheNode = CacheFlowNode<SimpleInput, SimpleOutput>(
            transformer = ::transform
        )
        
        // 4. 执行第一次，填充缓存
        val inputs1 = flow {
            emit(SimpleInput(1))
            emit(SimpleInput(2))
        }
        val results1 = cacheNode.transform(inputs1).toList()
        
        // 5. 验证缓存大小
        assertEquals(2, cacheNode.cacheSize())
        
        // 6. 清除缓存
        cacheNode.clearCache()
        
        // 7. 验证缓存已清空
        assertEquals(0, cacheNode.cacheSize())
    }
}
