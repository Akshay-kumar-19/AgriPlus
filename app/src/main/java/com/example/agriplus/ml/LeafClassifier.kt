package com.example.agriplus.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class LeafClassifier(context: Context) {

    private val interpreter: Interpreter
    private val imageSize = 300   // 🔥 FIXED (was 224)

    init {
        interpreter = Interpreter(loadModelFile(context))
    }

    private fun loadModelFile(context: Context): ByteBuffer {

        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun loadLabels(context: Context): List<String> {
        return context.assets.open("labels.txt")
            .bufferedReader()
            .readLines()
    }

    fun classify(bitmap: Bitmap, labels: List<String>): String {

        // 🔥 Resize to correct size (300x300)
        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)

        val byteBuffer =
            ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        resized.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        var index = 0

        for (pixel in pixels) {

            // 🔥 Azure style (0–255 float, no normalization)
            val r = (pixel shr 16 and 0xFF).toFloat()
            val g = (pixel shr 8 and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)

            index++
        }

        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(byteBuffer, output)

        var maxIndex = 0
        var maxValue = output[0][0]

        for (i in output[0].indices) {
            if (output[0][i] > maxValue) {
                maxValue = output[0][i]
                maxIndex = i
            }
        }

        return labels.getOrElse(maxIndex) { "Unknown" }
    }
}