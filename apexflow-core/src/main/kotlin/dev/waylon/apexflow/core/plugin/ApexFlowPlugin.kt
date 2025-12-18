package dev.waylon.apexflow.core.plugin

import dev.waylon.apexflow.core.ApexFlow

/**
 * ApexFlow 插件接口，用于扩展 ApexFlow 功能
 * 插件可以通过包装原始流程来添加额外功能，如日志、监控等
 */
interface ApexFlowPlugin {
    /**
     * 包装原始流程，添加插件功能
     */
    fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O>
}
