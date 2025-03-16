package com.mehrphilalethes.claytablet.view

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mehrphilalethes.claytablet.R
import com.mehrphilalethes.claytablet.view.TabletCanvasView
import kotlin.math.*
import androidx.core.graphics.toColorInt

class WedgePreviewView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var headScaleFactor = 0.13f
    var winkelhakenScaleFactor = 0.12f
    var bodyThickness = 8f
    var threshold = 148f

    private val headBitmap = BitmapFactory.decodeResource(resources, R.drawable.head)
    private val winkelBitmap = BitmapFactory.decodeResource(resources, R.drawable.winkelhaken)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor("#c05f3c".toColorInt())  // Clay background (for preview)

        val centerX = width / 2f

        // Example wedges explicitly shown:
        drawNailWedge(canvas, width * 0.2f, height * 0.2f, 0f, 300f)    // Long wedge
        drawNailWedge(canvas, width * 0.5f, height * 0.5f, 90f, 50f)     // Short wedge
        drawNailWedge(canvas, width * 0.7f, height * 0.5f, 90f, 90f)
        drawWinkelhaken(canvas, width * 0.27f, height * 0.7f, 0f) // Winkelhaken
    }

    private fun drawNailWedge(canvas: Canvas, x: Float, y: Float, rotation: Float, length: Float) {
        val wedgePaint = Paint().apply {
            strokeWidth = bodyThickness
            color = Color.BLACK
            strokeCap = Paint.Cap.SQUARE
            isAntiAlias = true
        }
        val endX = x + length * cos(Math.toRadians(rotation.toDouble())).toFloat()
        val endY = y + length * sin(Math.toRadians(rotation.toDouble())).toFloat()
        val scaleFactor = if (length < threshold) length / threshold else 1f
        val scale = headScaleFactor * scaleFactor
        canvas.drawLine(x, y, endX, endY, wedgePaint)

        // Head explicitly drawn
        val matrix = Matrix().apply {
            postScale(scale, scale, headBitmap.width / 2f, headBitmap.height / 2f)
            postTranslate(-headBitmap.width / 2f, -headBitmap.height / 2f)
            postRotate(rotation)
            postTranslate(x, y)
        }
        canvas.drawBitmap(headBitmap, matrix, null)
    }

    private fun drawWinkelhaken(canvas: Canvas, x: Float, y: Float, rotation: Float) {
        val matrix = Matrix().apply {
            postScale(winkelhakenScaleFactor, winkelhakenScaleFactor, winkelBitmap.width / 2f, winkelBitmap.height / 2f)
            postTranslate(-winkelBitmap.width / 2f, -winkelBitmap.height / 2f)
            postRotate(rotation)
            postTranslate(x, y)
        }
        canvas.drawBitmap(winkelBitmap, matrix, null)
    }

    fun updateParameters(headScale: Float, winkelScale: Float, thickness: Float, thresholdVal: Float) {
        headScaleFactor = headScale
        winkelhakenScaleFactor = winkelScale
        bodyThickness = thickness
        threshold = thresholdVal
        invalidate()
    }
}
