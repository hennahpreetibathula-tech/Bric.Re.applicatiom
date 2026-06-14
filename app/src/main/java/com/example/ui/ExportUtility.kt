package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.viewmodel.EvidenceCard
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.File
import java.io.FileOutputStream

object ExportUtility {

    /**
     * Generates a beautifully formatted scientific citation PDF and launches
     * a system Chooser to allow the user to save, share, email or print it.
     */
    fun sharePdfReport(
        context: Context,
        query: String,
        mode: String,
        confidence: String,
        finalAnswer: String,
        conflicts: String?,
        evidenceCards: List<EvidenceCard>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points
            var pageCount = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageCount).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                color = android.graphics.Color.rgb(10, 37, 64) // Indigo primary
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                color = android.graphics.Color.rgb(100, 110, 120)
            }

            val h1Paint = Paint().apply {
                isAntiAlias = true
                textSize = 13f
                color = android.graphics.Color.rgb(15, 76, 129) // Deep blue
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = android.graphics.Color.rgb(30, 30, 30) // Dark charcoal
            }

            val italicPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = android.graphics.Color.rgb(75, 85, 99) // Gray
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            }

            val quotePaint = Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                color = android.graphics.Color.rgb(60, 80, 100)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            }

            val dividerPaint = Paint().apply {
                color = android.graphics.Color.rgb(220, 225, 230)
                strokeWidth = 1f
            }

            var y = 50f
            val margin = 45f
            val contentWidth = pageWidth - (2 * margin)

            fun drawFooter() {
                val footerPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = android.graphics.Color.rgb(150, 150, 150)
                }
                canvas.drawText(
                    "Page $pageCount | Bric.Re Evidence Synthesis Export Engine",
                    margin,
                    pageHeight - 35f,
                    footerPaint
                )
                canvas.drawLine(margin, pageHeight - 48f, pageWidth - margin, pageHeight - 48f, dividerPaint)
            }

            fun checkPageBreak(requiredHeight: Float) {
                if (y + requiredHeight > pageHeight - margin - 40f) {
                    drawFooter()
                    pdfDocument.finishPage(page)
                    pageCount++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageCount).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin + 20f
                }
            }

            // Paragraph helper for text wrapping
            fun drawTextParagraph(text: String, paint: Paint, spacing: Float) {
                val lines = text.split("\n")
                for (line in lines) {
                    val words = line.split(" ")
                    var currentLine = StringBuilder()
                    for (word in words) {
                        if (word.isEmpty()) continue
                        val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
                        val width = paint.measureText(testLine)
                        if (width > contentWidth) {
                            checkPageBreak(spacing)
                            canvas.drawText(currentLine.toString(), margin, y, paint)
                            y += spacing
                            currentLine = StringBuilder(word)
                        } else {
                            currentLine.append(if (currentLine.isEmpty()) word else " $word")
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        checkPageBreak(spacing)
                        canvas.drawText(currentLine.toString(), margin, y, paint)
                        y += spacing
                    }
                }
            }

            // Report Header
            canvas.drawText("Bric.Re Synthesis Report", margin, y, titlePaint)
            y += 18f
            canvas.drawText(
                "Exported: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}",
                margin,
                y,
                subtitlePaint
            )
            y += 15f
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 20f

            // Query
            canvas.drawText("QUERY TOPIC", margin, y, h1Paint)
            y += 15f
            drawTextParagraph("\"$query\"", bodyPaint, 14f)
            y += 15f

            // Session Stats
            canvas.drawText("PIPELINE STATE METRICS", margin, y, h1Paint)
            y += 15f
            canvas.drawText("Analysis Mode Type: $mode", margin, y, bodyPaint)
            y += 14f
            canvas.drawText("System Confidence Rating: $confidence", margin, y, bodyPaint)
            y += 20f

            // Synthesis Result
            checkPageBreak(40f)
            canvas.drawText("SYNTHESIZED INTEL SUMMARY", margin, y, h1Paint)
            y += 15f
            drawTextParagraph(finalAnswer, bodyPaint, 15f)
            y += 20f

            // Contrasting Disagreements / Conflicts
            if (!conflicts.isNullOrBlank()) {
                checkPageBreak(40f)
                canvas.drawText("⚠️ ACTIVE CONTRADICTIONS ISOLATED", margin, y, h1Paint)
                y += 15f
                drawTextParagraph(conflicts, italicPaint, 14f)
                y += 20f
            }

            // Grounded Citation evidence
            if (evidenceCards.isNotEmpty()) {
                checkPageBreak(40f)
                canvas.drawText("GROUNDED CITATION EVIDENCE MATRIX", margin, y, h1Paint)
                y += 18f

                evidenceCards.forEachIndexed { index, card ->
                    checkPageBreak(60f)
                    val cardHeader = "[Cit-${index + 1}] ${card.title}"
                    canvas.drawText(cardHeader, margin, y, bodyPaint.apply { isFakeBoldText = true })
                    y += 13f
                    bodyPaint.apply { isFakeBoldText = false }

                    canvas.drawText(
                        "Source Repository: ${card.source} | Page: ${card.page}",
                        margin + 10f,
                        y,
                        subtitlePaint
                    )
                    y += 13f

                    canvas.drawText(
                        "Semantic Alignment: ${(card.score * 100).toInt()}% match | Class Balance Strength: ${card.strength}",
                        margin + 10f,
                        y,
                        italicPaint
                    )
                    y += 15f

                    // Quote indent
                    val oldX = margin
                    drawTextParagraph("\"${card.text}\"", quotePaint, 13f)
                    y += 12f
                }
            }

            // Finish up PDF file
            drawFooter()
            pdfDocument.finishPage(page)

            // Save to shared cache file
            val file = File(context.cacheDir, "BricRe_Intelligence_Export_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()

            // Trigger sharing context chooser
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BricRe RAG Intel Report - ${query.take(40)}")
                putExtra(Intent.EXTRA_TEXT, "Here is your exported structured PDF research report from BricRe.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Academic PDF Report"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export PDF Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates a fully structured JSON asset containing complete payload data
     * and streams it to external targets through a sharing sheet.
     */
    fun shareJsonReport(
        context: Context,
        query: String,
        mode: String,
        confidence: String,
        finalAnswer: String,
        conflicts: String?,
        evidenceCards: List<EvidenceCard>
    ) {
        try {
            val moshi = Moshi.Builder().build()
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)

            // Convert Cards to standard lists of Map properties
            val evidenceList = evidenceCards.map { card ->
                mapOf(
                    "title" to card.title,
                    "source" to card.source,
                    "page" to card.page,
                    "text" to card.text,
                    "strength" to card.strength,
                    "score" to card.score
                )
            }

            val payloadMap = mapOf(
                "application" to "Bric.Re Evidence RAG Engine",
                "exportTimestamp" to System.currentTimeMillis(),
                "queryText" to query,
                "researchMode" to mode,
                "confidenceScore" to confidence,
                "synthesizedAnswer" to finalAnswer,
                "contradictions" to (conflicts ?: "No contradictions identified in active corpus."),
                "groundedCitations" to evidenceList
            )

            val jsonString = adapter.indent("  ").toJson(payloadMap)

            val file = File(context.cacheDir, "BricRe_Intelligence_Payload_${System.currentTimeMillis()}.json")
            file.writeText(jsonString)

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BricRe RAG Payload JSON - ${query.take(40)}")
                putExtra(Intent.EXTRA_TEXT, "Here is your exported structured scientific research JSON payload from BricRe.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Academic JSON Payload"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export JSON Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
