package dev.waylon.apexflow.core.node

import kotlinx.coroutines.flow.Flow

/**
 * 流程节点接口，代表工作流中的单个转换节点，用于执行特定的转换逻辑
 * 符合Clean Architecture的核心层，不依赖任何外部实现
 * @param I 输入类型
 * @param O 输出类型
 */
interface FlowNode<I, O> {
    /**
     * 将输入Flow转换为输出Flow，执行单个节点的转换逻辑
     */
    fun transform(input: Flow<I>): Flow<O>
}
