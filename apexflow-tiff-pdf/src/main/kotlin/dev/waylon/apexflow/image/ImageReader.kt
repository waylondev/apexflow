package dev.waylon.apexflow.image

import java.awt.image.BufferedImage
import kotlinx.coroutines.flow.Flow

/**
 * ApexFlow图像读取器接口
 *
 * 定义了从输入源读取图像的通用方法，支持流式处理
 */
interface ApexImageReader {
    /**
     * 读取图像并返回BufferedImage流
     *
     * @return Flow<BufferedImage> 图像流
     */
    fun read(): Flow<BufferedImage>
}
