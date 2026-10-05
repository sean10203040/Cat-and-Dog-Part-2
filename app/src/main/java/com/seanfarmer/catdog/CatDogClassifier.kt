package com.seanfarmer.catdog

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** All calls, including close(), must be made on the same single worker thread. */
class CatDogClassifier(context: Context) : Closeable {
    private val interpreter: Interpreter

    init {
        val bytes = context.assets.open("cat_dog.tflite").use { it.readBytes() }
        val model = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        model.put(bytes); model.rewind()
        val instance = Interpreter(model, Interpreter.Options().setNumThreads(2))
        try {
            require(instance.inputTensorCount == 1 && instance.outputTensorCount == 1) {
                "Expected one input and one output. Use the Part 1 cat_dog.tflite model."
            }
            val input = instance.getInputTensor(0)
            require(input.dataType() == DataType.FLOAT32 &&
                input.shape().contentEquals(intArrayOf(1, 128, 128, 3))) {
                "Expected float32 input [1, 128, 128, 3]."
            }
            val output = instance.getOutputTensor(0)
            require(output.dataType() == DataType.FLOAT32 &&
                output.shape().contentEquals(intArrayOf(1, 1))) {
                "Expected float32 output [1, 1], the probability of dog."
            }
        } catch (error: Exception) {
            instance.close()
            throw error
        }
        interpreter = instance
    }

    fun classify(bitmap: Bitmap): Pair<Float, Float> {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val input = ModelMath.toInput(pixels, bitmap.width, bitmap.height)
        val output = Array(1) { FloatArray(1) }
        interpreter.run(input, output)
        return ModelMath.percentages(output[0][0])
    }

    override fun close() { interpreter.close() }
}
