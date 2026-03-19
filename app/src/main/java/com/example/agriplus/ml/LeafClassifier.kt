package com.example.agriplus.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LeafClassifier(context: Context) {

    private var interpreter: Interpreter
    private val imageSize = 224

    init {
        interpreter = Interpreter(loadModelFile(context))
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {

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

        val imageSize = 224

        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)

        val byteBuffer =
            ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        resized.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        var index = 0

        for (pixel in pixels) {

            // 🔥 IMPORTANT NORMALIZATION FIX
            val r = ((pixel shr 16 and 0xFF) - 127.5f) / 127.5f
            val g = ((pixel shr 8 and 0xFF) - 127.5f) / 127.5f
            val b = ((pixel and 0xFF) - 127.5f) / 127.5f

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

        return labels[maxIndex]
    }
}