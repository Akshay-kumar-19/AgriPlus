package com.example.agriplus.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class LeafClassifier(private val context: Context) {

    private var interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile())
    }

    /* ---------- LOAD MODEL ---------- */
    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    /* ---------- LOAD LABELS ---------- */
    fun loadLabels(context: Context): List<String> {
        return context.assets.open("labels.txt")
            .bufferedReader()
            .readLines()
    }

    /* ---------- CLASSIFY ---------- */
    fun classify(bitmap: Bitmap, labels: List<String>): Pair<String, Float> {

        val inputSize = 300   // 🔥 FIXED (matches your model)

        val resizedBitmap =
            Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val byteBuffer =
            ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(
            intValues,
            0,
            inputSize,
            0,
            0,
            inputSize,
            inputSize
        )

        var pixelIndex = 0

        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {

                val pixelValue = intValues[pixelIndex++]

                val r = (pixelValue shr 16 and 0xFF) / 255f
                val g = (pixelValue shr 8 and 0xFF) / 255f
                val b = (pixelValue and 0xFF) / 255f

                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }

        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(byteBuffer, output)

        val confidences = output[0]

        var maxIndex = 0
        var maxConfidence = confidences[0]

        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxIndex = i
            }
        }

        return Pair(labels[maxIndex], maxConfidence)
    }
}