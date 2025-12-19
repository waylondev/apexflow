package dev.waylon.apexflow.image

import java.awt.image.BufferedImage
import kotlinx.coroutines.flow.Flow

/**
 * ApexFlow图像写入器接口
 *
 * 定义了向输出目标写入图像的通用方法，支持流式处理
 */
interface ApexImageWriter {
    /**
     * 写入图像流到输出目标
     *
     * @param data BufferedImage流
     */
    suspend fun write(data: Flow<BufferedImage>)
}
