package dev.waylon.apexflow.core

import kotlinx.coroutines.flow.Flow

/**
 * 顶层流程接口，代表一个完整的工作流，用于编排多个FlowNode
 * 符合Clean Architecture的核心层，不依赖任何外部实现
 * @param I 输入类型
 * @param O 输出类型
 */
interface ApexFlow<I, O> {
    /**
     * 将输入Flow转换为输出Flow，执行完整的工作流
     */
    fun transform(input: Flow<I>): Flow<O>
    
    /**
     * 嵌套类型别名，用于简化复杂的Flow类型声明
     * 使用Kotlin 2.3.0的嵌套类型别名特性
     */
    typealias Single<I> = ApexFlow<I, I>
    typealias Chain<I, O> = ApexFlow<I, O>
    typealias Composed<I, M, O> = (ApexFlow<I, M>, ApexFlow<M, O>) -> ApexFlow<I, O>
}
