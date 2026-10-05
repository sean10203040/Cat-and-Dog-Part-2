package com.seanfarmer.catdog

import org.junit.Assert.*
import org.junit.Test

class ModelMathTest {
    @Test fun dogIsSigmoidAndCatIsComplement() {
        val result = ModelMath.percentages(0.8f)
        assertEquals(80f, result.first, 0.001f)
        assertEquals(20f, result.second, 0.001f)
        for (p in listOf(0f, 0.1f, 0.5f, 1f)) {
            val (dog, cat) = ModelMath.percentages(p)
            assertEquals(100f, dog + cat, 0.001f)
        }
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidModelOutputIsRejected() { ModelMath.percentages(Float.NaN) }

    @Test fun solidRgbIsNotNormalizedTwice() {
        val buffer = ModelMath.toInput(intArrayOf(0xff0a141e.toInt()), 1, 1)
        repeat(128 * 128) {
            assertEquals(10f, buffer.float, 0f)
            assertEquals(20f, buffer.float, 0f)
            assertEquals(30f, buffer.float, 0f)
        }
        assertEquals(0, buffer.remaining())
    }
    @Test fun bilinearHalfPixelInterpolation() {
        val pixels = intArrayOf(0xff000000.toInt(), 0xffffffff.toInt())
        val buffer = ModelMath.toInput(pixels, 2, 1)
        fun red(x: Int) = buffer.getFloat(x * 3 * 4)
        assertEquals(0f, red(0), 0f)
        assertEquals(255f, red(127), 0f)
        assertEquals(0.4921875f * 255, red(63), 0.0001f)
        assertEquals(0.5078125f * 255, red(64), 0.0001f)
    }
}
