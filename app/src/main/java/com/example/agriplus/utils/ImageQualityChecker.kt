package com.example.agriplus.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

data class QualityCheckResult(
    val isValid: Boolean,
    val warning: String? = null,
    val imageHash: Long = 0L
)

object ImageQualityChecker {

    /**
     * Comprehensive Image Quality Pre-Scan Evaluation:
     * 1. Blur / Sharpness check
     * 2. Brightness (Underexposed / Overexposed)
     * 3. Duplicate scan detection
     */
    fun evaluate(
        bitmap: Bitmap,
        recentHashes: List<Long> = emptyList()
    ): QualityCheckResult {
        // Downsample for rapid evaluation without UI hitching
        val sampleSize = 64
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, false)

        var totalLuminance = 0.0
        var totalVariance = 0.0
        var hash = 0L

        val pixels = IntArray(sampleSize * sampleSize)
        scaled.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)

        // 1. Calculate Average Luminance & Simple Perceptual Hash
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)
            val lum = 0.299 * r + 0.587 * g + 0.114 * b
            totalLuminance += lum
        }

        val avgLuminance = totalLuminance / (sampleSize * sampleSize)

        // 64-bit hash from average luminance
        for (i in 0 until 64) {
            val p = pixels[i * (sampleSize * sampleSize / 64)]
            val lum = 0.299 * Color.red(p) + 0.587 * Color.green(p) + 0.114 * Color.blue(p)
            if (lum > avgLuminance) {
                hash = hash or (1L shl i)
            }
        }

        // 2. Brightness Validation
        if (avgLuminance < 42.0) {
            return QualityCheckResult(
                isValid = false,
                warning = "⚠️ Image is too dark (Luminance: ${avgLuminance.toInt()}/255). Ensure adequate daylight or turn on your flashlight.",
                imageHash = hash
            )
        }
        if (avgLuminance > 228.0) {
            return QualityCheckResult(
                isValid = false,
                warning = "⚠️ Image is overexposed or has flash glare (Luminance: ${avgLuminance.toInt()}/255). Avoid direct flash glare on the leaf.",
                imageHash = hash
            )
        }

        // 3. Blur / Sharpness Gradient Check (Neighbor differences)
        var gradientSum = 0.0
        for (y in 0 until sampleSize - 1) {
            for (x in 0 until sampleSize - 1) {
                val pCurr = pixels[y * sampleSize + x]
                val pRight = pixels[y * sampleSize + (x + 1)]
                val pDown = pixels[(y + 1) * sampleSize + x]

                val lumCurr = 0.299 * Color.red(pCurr) + 0.587 * Color.green(pCurr) + 0.114 * Color.blue(pCurr)
                val lumRight = 0.299 * Color.red(pRight) + 0.587 * Color.green(pRight) + 0.114 * Color.blue(pRight)
                val lumDown = 0.299 * Color.red(pDown) + 0.587 * Color.green(pDown) + 0.114 * Color.blue(pDown)

                gradientSum += abs(lumCurr - lumRight) + abs(lumCurr - lumDown)
            }
        }

        val avgGradient = gradientSum / ((sampleSize - 1) * (sampleSize - 1))
        if (avgGradient < 5.2) {
            return QualityCheckResult(
                isValid = false,
                warning = "⚠️ Image appears blurry. Hold camera steady and tap to focus on the coffee leaf.",
                imageHash = hash
            )
        }

        // 4. Duplicate Check
        if (recentHashes.contains(hash)) {
            return QualityCheckResult(
                isValid = false,
                warning = "ℹ️ Duplicate leaf image detected. You have recently submitted this exact scan.",
                imageHash = hash
            )
        }

        return QualityCheckResult(
            isValid = true,
            warning = null,
            imageHash = hash
        )
    }
}
