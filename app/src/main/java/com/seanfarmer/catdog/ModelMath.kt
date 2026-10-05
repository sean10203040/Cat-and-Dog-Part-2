package com.seanfarmer.catdog

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.floor

/** Pure math separated from Android and LiteRT so preprocessing can be unit tested. */
object ModelMath {
    const val SIDE = 128
    fun percentages(dogProbability: Float): Pair<Float, Float> {
        require(dogProbability.isFinite() && dogProbability in 0f..1f) {
            "Expected one sigmoid probability in [0, 1]."
        }
        return Pair(dogProbability * 100f, (1f - dogProbability) * 100f)
    }

    /** Matches TF resize bilinear half-pixel coordinates, without integer rounding.
     * NHWC RGB float32, range 0..255. DO NOT divide by 255: model has Rescaling.
     */
    fun toInput(pixels: IntArray, width: Int, height: Int): ByteBuffer {
        require(width > 0 && height > 0 && pixels.size == width * height)
        val buffer = ByteBuffer.allocateDirect(SIDE * SIDE * 3 * 4)
            .order(ByteOrder.nativeOrder())
        for (y in 0 until SIDE) {
            val sy = ((y + 0.5f) * height / SIDE - 0.5f).coerceIn(0f, (height - 1).toFloat())
            val y0 = floor(sy).toInt(); val y1 = (y0 + 1).coerceAtMost(height - 1)
            val fy = sy - y0
            for (x in 0 until SIDE) {
                val sx = ((x + 0.5f) * width / SIDE - 0.5f).coerceIn(0f, (width - 1).toFloat())
                val x0 = floor(sx).toInt(); val x1 = (x0 + 1).coerceAtMost(width - 1)
                val fx = sx - x0
                for (shift in intArrayOf(16, 8, 0)) {
                    fun channel(xx: Int, yy: Int) = ((pixels[yy * width + xx] ushr shift) and 255).toFloat()
                    val top = channel(x0, y0) * (1f - fx) + channel(x1, y0) * fx
                    val bottom = channel(x0, y1) * (1f - fx) + channel(x1, y1) * fx
                    buffer.putFloat(top * (1f - fy) + bottom * fy)
                }
            }
        }
        buffer.rewind()
        return buffer
    }
}
