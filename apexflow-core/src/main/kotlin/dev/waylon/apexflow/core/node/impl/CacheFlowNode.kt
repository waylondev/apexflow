package dev.waylon.apexflow.core.node.impl

import dev.waylon.apexflow.core.node.FlowNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

/**
 * 缓存节点，用于缓存转换结果
 * 典型的FlowNode实现，用于演示状态管理和缓存功能
 * @param I 输入类型
 * @param O 输出类型
 * @property transformer 转换函数
 */
class CacheFlowNode<I, O>(
    private val transformer: suspend (I) -> O
) : FlowNode<I, O> {
    
    // 使用ConcurrentHashMap作为线程安全的缓存
    private val cache = ConcurrentHashMap<I, O>()
    
    override fun transform(input: Flow<I>): Flow<O> {
        return input.map { inputData ->
            // 检查缓存，如果存在则直接返回
            cache[inputData] ?: run {
                // 缓存不存在，执行转换并缓存结果
                val result = transformer(inputData)
                cache[inputData] = result
                result
            }
        }
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * 获取缓存大小
     */
    fun cacheSize(): Int {
        return cache.size
    }
}
