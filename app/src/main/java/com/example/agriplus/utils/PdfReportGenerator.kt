package com.example.agriplus.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.agriplus.data.DiseaseRepository
import com.example.agriplus.data.HistoryItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateScanReportPdf(
        context: Context,
        item: HistoryItem,
        plantationName: String = "AgriPlus Coffee Estate"
    ): File? {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in PostScript points (72 points/inch)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paints
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Banner
        paint.color = Color.rgb(20, 60, 40) // Forest coffee green
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

        // Title
        textPaint.color = Color.WHITE
        textPaint.textSize = 22f
        textPaint.isFakeBoldText = true
        canvas.drawText("AgriPlus • Coffee Health Scan Report", 24f, 44f, textPaint)

        // Subtitle
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(180, 230, 195)
        canvas.drawText("Automated Disease Diagnosis & Agronomic Recommendation System", 24f, 68f, textPaint)

        // Meta info line (Date & Report ID)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val scanDate = if (item.timestamp > 0) dateFormat.format(Date(item.timestamp)) else dateFormat.format(Date())
        val reportId = "REP-" + (if (item.timestamp > 0) item.timestamp.toString().takeLast(6) else "001")

        paint.color = Color.rgb(240, 245, 240)
        canvas.drawRect(24f, 108f, (pageWidth - 24).toFloat(), 148f, paint)

        textPaint.color = Color.rgb(40, 50, 45)
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        canvas.drawText("ESTATE: $plantationName", 36f, 132f, textPaint)
        canvas.drawText("DATE: $scanDate", 250f, 132f, textPaint)
        canvas.drawText("ID: $reportId", 450f, 132f, textPaint)

        var currentY = 175f

        // 2. Leaf Image and Diagnostic Badge
        var bitmap: Bitmap? = null
        try {
            if (item.imageBase64.isNotEmpty()) {
                val decoded = Base64.decode(item.imageBase64, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            }
        } catch (e: Exception) {
            // ignore
        }

        val imageBoxWidth = 140f
        val imageBoxHeight = 140f
        val imageX = 24f

        if (bitmap != null) {
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = RectF(imageX, currentY, imageX + imageBoxWidth, currentY + imageBoxHeight)

            // Draw border
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawRoundRect(dstRect, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        } else {
            // Placeholder box
            paint.color = Color.rgb(230, 230, 230)
            canvas.drawRoundRect(imageX, currentY, imageX + imageBoxWidth, currentY + imageBoxHeight, 8f, 8f, paint)
            textPaint.color = Color.GRAY
            textPaint.textSize = 12f
            canvas.drawText("Leaf Photo", imageX + 35f, currentY + 75f, textPaint)
        }

        // Diagnostic Right Side
        val detailsX = imageX + imageBoxWidth + 24f
        val diseaseInfo = DiseaseRepository.getInfo(item.result)
        val isHealthy = item.result.lowercase().contains("healthy")
        val confPercent = (item.confidence * 100).toInt()

        textPaint.color = Color.rgb(20, 80, 50)
        textPaint.textSize = 16f
        textPaint.isFakeBoldText = true
        canvas.drawText(diseaseInfo.name, detailsX, currentY + 24f, textPaint)

        textPaint.color = Color.DKGRAY
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = false
        canvas.drawText("Prediction Confidence: $confPercent%", detailsX, currentY + 48f, textPaint)

        // Progress bar for confidence
        val barWidth = 200f
        val barHeight = 8f
        paint.color = Color.rgb(220, 225, 220)
        canvas.drawRoundRect(detailsX, currentY + 56f, detailsX + barWidth, currentY + 56f + barHeight, 4f, 4f, paint)

        paint.color = if (isHealthy) Color.rgb(46, 125, 50) else Color.rgb(216, 67, 21)
        val fillWidth = barWidth * (item.confidence.toFloat().coerceIn(0f, 1f))
        canvas.drawRoundRect(detailsX, currentY + 56f, detailsX + fillWidth, currentY + 56f + barHeight, 4f, 4f, paint)

        textPaint.color = Color.rgb(70, 70, 70)
        textPaint.textSize = 10f
        canvas.drawText("Health Status: " + if (isHealthy) "Foliage in Good Condition" else "Intervention Required", detailsX, currentY + 85f, textPaint)
        canvas.drawText("Target Crop: Coffea arabica / canephora", detailsX, currentY + 105f, textPaint)
        canvas.drawText("Diagnostic Engine: AgriPlus AI Vision API", detailsX, currentY + 125f, textPaint)

        currentY += imageBoxHeight + 35f

        // 3. Sections: Cause, Symptoms, Treatment, Prevention
        fun drawSection(title: String, content: String, titleColor: Int) {
            paint.color = Color.rgb(245, 248, 245)
            val startY = currentY
            val sectionWidth = (pageWidth - 48).toFloat()

            // Header text
            textPaint.color = titleColor
            textPaint.textSize = 12f
            textPaint.isFakeBoldText = true
            canvas.drawText(title, 24f, currentY, textPaint)
            currentY += 16f

            // Multi-line wrap
            textPaint.color = Color.rgb(50, 50, 50)
            textPaint.textSize = 9.5f
            textPaint.isFakeBoldText = false

            val words = content.split(" ")
            var line = ""
            val maxWidth = sectionWidth - 16f

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val measure = textPaint.measureText(testLine)
                if (measure > maxWidth) {
                    canvas.drawText(line, 28f, currentY, textPaint)
                    currentY += 14f
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, 28f, currentY, textPaint)
                currentY += 14f
            }

            currentY += 12f
        }

        drawSection("1. DIAGNOSED CAUSE & PATHOGENESIS", diseaseInfo.cause, Color.rgb(180, 50, 50))
        drawSection("2. OBSERVABLE SYMPTOMS IN FIELD", diseaseInfo.symptoms, Color.rgb(190, 110, 0))
        drawSection("3. RECOMMENDED TREATMENT & CHEMICAL / BIOLOGICAL DOSAGE", diseaseInfo.treatment, Color.rgb(30, 100, 50))

        val generalPrevention = "Regularly inspect leaf undersides during morning hours. Maintain overhead shade at 50-60% canopy penetration. Clear low-lying water channels to avoid fungal root asphyxiation. Calibrate foliar spray nozzles to ensure complete under-leaf stomatal coverage."
        drawSection("4. AGRONOMIC PREVENTION & CULTURAL PRACTICES", generalPrevention, Color.rgb(30, 80, 120))

        // 4. Footer & Signature
        val footerY = (pageHeight - 55).toFloat()
        paint.color = Color.rgb(230, 235, 230)
        canvas.drawLine(24f, footerY, (pageWidth - 24).toFloat(), footerY, paint)

        textPaint.color = Color.GRAY
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        canvas.drawText("Generated by AgriPlus Decision Support System • For scientific farm management use only.", 24f, footerY + 18f, textPaint)
        canvas.drawText("Page 1 of 1", (pageWidth - 75).toFloat(), footerY + 18f, textPaint)

        pdfDocument.finishPage(page)

        // Save File
        val reportsDir = File(context.cacheDir, "scan_reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "AgriPlus_${item.result}_${item.timestamp}.pdf"
        val pdfFile = File(reportsDir, fileName)

        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            pdfDocument.close()
            return null
        }
    }

    fun sharePdfReport(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "AgriPlus Coffee Health Scan Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is the diagnostic scan report generated by AgriPlus.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Coffee Health Report via..."))
    }

    fun viewPdfReport(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            sharePdfReport(context, pdfFile)
        }
    }
}
