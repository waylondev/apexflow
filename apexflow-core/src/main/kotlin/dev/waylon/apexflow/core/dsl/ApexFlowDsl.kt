package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * 顶层 DSL 函数，用于创建 ApexFlow 流程
 * 使用 Kotlin 2.3.0 风格的现代设计
 */
fun <I, O> apexFlow(block: Flow<I>.() -> Flow<O>): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            return input.block()
        }
    }
}

/**
 * 扩展函数：带协程调度器的转换操作
 * 允许为每个节点指定运行的协程上下文
 */
fun <I, O> Flow<I>.transformOn(
    dispatcher: CoroutineDispatcher,
    block: suspend (I) -> O
): Flow<O> {
    return this.flowOn(dispatcher).map(block)
}

/**
 * 扩展函数：IO 密集型转换操作
 */
fun <I, O> Flow<I>.transformOnIO(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.IO, block)
}

/**
 * 扩展函数：CPU 密集型转换操作
 */
fun <I, O> Flow<I>.transformOnDefault(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Default, block)
}

/**
 * 扩展函数：无限制调度器的转换操作
 */
fun <I, O> Flow<I>.transformOnUnconfined(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Unconfined, block)
}

/**
 * 扩展函数：使用插件包装流程
 */
fun <I, O> ApexFlow<I, O>.withPlugin(plugin: ApexFlowPlugin): ApexFlow<I, O> {
    return plugin.wrap(this)
}

