package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory

/**
 * 日志插件，用于添加日志功能
 * 直接使用 SLF4J API，不依赖具体的日志实现
 */
class LoggingPlugin(private val loggerName: String = "dev.waylon.apexflow") : ApexFlowPlugin {
    
    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        // 创建 SLF4J 日志记录器
        val logger = LoggerFactory.getLogger(loggerName)
        
        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                // 使用 SLF4J 记录日志
                return input
                    .onStart { logger.info("Flow execution started") }
                    .onEach { data: I -> logger.debug("Processing input: {}", data) }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onEach { data: O -> logger.debug("Processed output: {}", data) }
                    .onCompletion { exception: Throwable? ->
                        if (exception == null) {
                            logger.info("Flow execution completed successfully")
                        } else {
                            logger.error("Flow execution completed with exception", exception)
                        }
                    }
            }
        }
    }
}

/**
 * 扩展函数：添加日志插件
 * 使用 SLF4J API，不依赖具体的日志实现
 */
fun <I, O> ApexFlow<I, O>.withLogging(loggerName: String = "dev.waylon.apexflow"): ApexFlow<I, O> {
    return LoggingPlugin(loggerName).wrap(this)
}
