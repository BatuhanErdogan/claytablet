package com.mehrphilalethes.claytablet.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.mehrphilalethes.claytablet.model.WedgeSymbol
import com.mehrphilalethes.claytablet.model.WedgeType
import kotlin.math.*
import com.mehrphilalethes.claytablet.R
import com.mehrphilalethes.claytablet.model.ActionType
import com.mehrphilalethes.claytablet.model.TabletAction
import java.util.Stack


class TabletCanvasView(context: Context, attrs: AttributeSet?) : View(context, attrs), GestureDetector.OnGestureListener {

    // --- Attributes ---

    private val wedges = mutableListOf<WedgeSymbol>()

    private var startX = 0f
    private var startY = 0f
    private var shadowEndX = 0f
    private var shadowEndY = 0f

    private var headBitmap = BitmapFactory.decodeResource(resources, R.drawable.head)
    private var winkelBitmap = BitmapFactory.decodeResource(resources, R.drawable.winkelhaken)
    private var clayBitmap = BitmapFactory.decodeResource(resources, R.drawable.clay_texture)

    private val undoStack = Stack<TabletAction>()
    private val redoStack = Stack<TabletAction>()

    var headScaleFactor = 0.13f
    var winkelhakenScaleFactor = 0.12f
    var threshold = 148f
    var bodyThickness = 8f
    var eraserRadius = 50f
    var deviceRotationAngle = 0f // 0°, 90°, 180°, 270°
    var scrollOffsetY = 0f

    var eraserMode = false
    var snapMode = true
    var scrollMode = false

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (scrollMode) {
                scrollOffsetY += -distanceY
                invalidate()
                return true
            }
            return false
        }
    })

    private val shadowPaint = Paint().apply {
        color = Color.HSVToColor(floatArrayOf(0f, 0.718f, 0.459f))
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        alpha = 120
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        val matrix = Matrix()

        // Use context to get resources
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val bitmapWidth = clayBitmap.width.toFloat()

        val scaleFactor = screenWidth / bitmapWidth  // Scale to match the screen width

        // Create a shader with REPEAT only in the vertical direction
        shader = BitmapShader(clayBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

        // Apply the scaling transformation
        matrix.setScale(scaleFactor, scaleFactor)
        shader.setLocalMatrix(matrix)
    }


    // --- Default Functions ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(0f, scrollOffsetY)

        // Draw clay texture as background
        canvas.drawRect(0f, -scrollOffsetY, width.toFloat(), height - scrollOffsetY, backgroundPaint)

        // Draw existing wedges
        wedges.forEach { wedge ->
            drawWedge(canvas, wedge)
        }

        // Draw shadow line while swiping
        if (shadowEndX != startX || shadowEndY != startY) {
            canvas.drawLine(startX, startY - scrollOffsetY, shadowEndX, shadowEndY - scrollOffsetY, shadowPaint)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (scrollMode) {
            gestureDetector.onTouchEvent(event)
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (eraserMode) {
                    eraseWedgesAt(event.x, event.y)
                } else {
                    startX = event.x
                    startY = event.y
                    shadowEndX = startX
                    shadowEndY = startY
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (eraserMode) {
                    eraseWedgesAt(event.x, event.y)
                } else {
                    shadowEndX = guidedX(startX, startY, event.x, event.y)
                    shadowEndY = guidedY(startX, startY, event.x, event.y)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val dx = shadowEndX - startX
                val dy = shadowEndY - startY
                val wedge : WedgeSymbol

                if (hypot(dx, dy) < 15f) {
                    performClick()  // single tap (winkelhaken)
                    wedge = WedgeSymbol(WedgeType.WINKELHAKEN, startX, startY - scrollOffsetY, -deviceRotationAngle, 0f, 0f, winkelhakenScaleFactor, 0f)
                } else {
                    val wedgeType = determineWedgeType(dx, dy)
                    val rotation = calculateRotation(dx, dy)
                    wedge = WedgeSymbol(wedgeType, startX, startY - scrollOffsetY, rotation, hypot(dx, dy), bodyThickness, headScaleFactor, threshold)
                }

                if (!eraserMode) {
                    addWedge(wedge)
                }
                shadowEndX = startX
                shadowEndY = startY
                invalidate()
            }
        }
        return true
    }

    // Call explicitly when adding a wedge:
    fun addWedge(wedge: WedgeSymbol) {
        wedges.add(wedge)
        undoStack.push(TabletAction(ActionType.ADD, listOf(wedge)))
        redoStack.clear()
        invalidate()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val action = undoStack.pop()
            when (action.actionType) {
                ActionType.ADD -> wedges.removeAll(action.wedges)
                ActionType.ERASE -> wedges.addAll(action.wedges)
                ActionType.CLEAR -> wedges.addAll(action.wedges)
            }
            redoStack.push(action)
            invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.pop()
            when (action.actionType) {
                ActionType.ADD -> wedges.addAll(action.wedges)
                ActionType.ERASE -> wedges.removeAll(action.wedges)
                ActionType.CLEAR -> wedges.clear()
            }
            undoStack.push(action)
            invalidate()
        }
    }

    private fun eraseWedgesAt(x: Float, y: Float) {
        val erasedWedges = wedges.filter {
            hypot(it.x - x, it.y + scrollOffsetY - y) <= eraserRadius
        }
        if (erasedWedges.isNotEmpty()) {
            wedges.removeAll(erasedWedges)
            undoStack.push(TabletAction(ActionType.ERASE, erasedWedges))
            redoStack.clear()
            invalidate()
        }
    }

    fun clearTablet() {
        if (wedges.isNotEmpty()) {
            undoStack.push(TabletAction(ActionType.CLEAR, wedges.toList()))
            redoStack.clear()
            wedges.clear()
            invalidate()
        }
    }

    // TODO: UPDATE THIS!
    private fun determineWedgeType(dx: Float, dy: Float): WedgeType {
        val angleDegrees = Math.toDegrees(atan2(dy, dx).toDouble()).let { if (it < 0) it + 360 else it }
        return when {
            angleInRange(angleDegrees = angleDegrees, target = 0.0) || angleInRange(angleDegrees, 180.0) -> WedgeType.HORIZONTAL
            angleInRange(angleDegrees, 90.0) || angleInRange(angleDegrees, 270.0) -> WedgeType.VERTICAL
            angleInRange(angleDegrees, 45.0) || angleInRange(angleDegrees, 225.0) -> WedgeType.DIAGONALD // NW-SE
            angleInRange(angleDegrees, 135.0) || angleInRange(angleDegrees, 315.0) -> WedgeType.DIAGONALU
            else -> WedgeType.HORIZONTAL
        }
    }

    private fun angleInRange(angleDegrees: Double, target: Double, tolerance: Double = 30.0): Boolean {
        val diff = abs(angleDegrees - target)
        return diff <= tolerance || abs(diff - 360.0) <= tolerance
    }

    private fun calculateRotation(dx: Float, dy: Float): Float {
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    private fun angleCloseCardinal(angle: Double): Boolean {
        if (!snapMode) {
            val correctedAngle = angle + 180.0
            return correctedAngle % 90.0 <= 15.0 || correctedAngle % 90.0 >= 75.0
        } else {
            return true
        }
    }

    private fun guidedX(x1: Float, y1: Float, x: Float, y: Float): Float {
        val angle = atan2(y - y1, x - x1)
        val angleInDegrees = Math.toDegrees(angle.toDouble())
        if (angleCloseCardinal(angleInDegrees)) {
            val roundedAngle = (round(angle / (PI / 4)) * (PI / 4)).toFloat()
            val length = hypot(x - x1, y - y1)
            return (x1 + length * cos(roundedAngle))
        } else {
            return x
        }
    }

    private fun guidedY(x1: Float, y1: Float, x: Float, y: Float): Float {
        val angle = atan2(y - y1, x - x1)
        val angleInDegrees = Math.toDegrees(angle.toDouble())
        if (angleCloseCardinal(angleInDegrees)) {
            val roundedAngle = (round(angle / (PI / 4)) * (PI / 4)).toFloat()
            val length = hypot(x - x1, y - y1)
            return (y1 + length * sin(roundedAngle))
        } else {
            return y
        }
    }

    private fun drawWedge(canvas: Canvas, wedge: WedgeSymbol) {
        if (wedge.type == WedgeType.WINKELHAKEN) {
            drawWinkelhaken(canvas, wedge)
        } else {
            drawNailWedge(canvas, wedge)
        }
    }

    private fun drawWinkelhaken(canvas: Canvas, wedge: WedgeSymbol) {
        val scale = wedge.size  // Adjust as needed
        val matrix = Matrix().apply {
            postScale(scale, scale, winkelBitmap.width / 2f, winkelBitmap.height / 2f)
            postTranslate(-winkelBitmap.width / 2f, -winkelBitmap.height / 2f)
            postRotate(wedge.rotation)
            postTranslate(wedge.x, wedge.y)
        }
        canvas.drawBitmap(winkelBitmap, matrix, null)
    }

    private fun drawNailWedge(canvas: Canvas, wedge: WedgeSymbol) {
        val wedgeLength = wedge.length
        val wedgePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = wedge.thickness
            strokeCap = Paint.Cap.SQUARE
            isAntiAlias = true
        }
        // Scale factors
        val headDefaultScale = wedge.size
        val threshVal = wedge.thresholdSensitivity
        val scaleFactor = if (wedgeLength < threshVal) wedgeLength / threshVal else 1f
        val headScale = headDefaultScale * scaleFactor

        val radianAngle = Math.toRadians(wedge.rotation.toDouble())
        val endX = wedge.x + wedgeLength * cos(radianAngle).toFloat()
        val endY = wedge.y + wedgeLength * sin(radianAngle).toFloat()
        canvas.drawLine(wedge.x, wedge.y, endX, endY, wedgePaint)

        val headMatrix = Matrix().apply {
            postScale(headScale, headScale, headBitmap.width / 2f, headBitmap.height / 2f)
            postTranslate(-headBitmap.width / 2f, -headBitmap.height / 2f)
            postRotate(wedge.rotation)
            postTranslate(wedge.x, wedge.y)
        }
        canvas.drawBitmap(headBitmap, headMatrix, null)
    }

    fun loadPreferences(context: Context) {
        val prefs = context.getSharedPreferences("tablet_prefs", Context.MODE_PRIVATE)
        headScaleFactor = prefs.getInt("headSize", 13) / 100f
        winkelhakenScaleFactor = prefs.getInt("winkelSize", 12) / 100f
        bodyThickness = prefs.getInt("bodyThickness", 8).toFloat()
        threshold = prefs.getInt("threshold", 148).toFloat()
        invalidate()
    }

    fun setDeviceRotation(angle: Float) {
        deviceRotationAngle = angle
        //invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onShowPress(e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLongPress(e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        TODO("Not yet implemented")
    }
}
