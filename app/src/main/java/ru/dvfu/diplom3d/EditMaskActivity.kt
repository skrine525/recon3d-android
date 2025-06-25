package ru.dvfu.diplom3d

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.io.File

class EditMaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val planPath = intent.getStringExtra("plan_path")
        val maskPath = intent.getStringExtra("mask_path")
        val planBitmap = BitmapFactory.decodeFile(planPath)
        val maskBitmap = BitmapFactory.decodeFile(maskPath)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Редактирование маски"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)
        setSupportActionBar(toolbar)
        val maskEditView = MaskEditView(this, planBitmap, maskBitmap)
        layout.addView(maskEditView)
        setContentView(layout)
    }
}

class MaskEditView(context: Context, val planBitmap: Bitmap, val maskBitmap: Bitmap) : android.view.View(context) {
    private val paint = Paint()
    private val matrix = Matrix()
    private var scale = 1f
    private var minScale = 1f
    private var translateX = 0f
    private var translateY = 0f
    private var isInitialized = false
    private var viewW = 0f
    private var viewH = 0f
    private var startFocusX = 0f
    private var startFocusY = 0f
    private var startTranslateX = 0f
    private var startTranslateY = 0f
    private var startScale = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastTranslateX = 0f
    private var lastTranslateY = 0f
    private var isPanning = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewW = w.toFloat()
        viewH = h.toFloat()
        isInitialized = false
        invalidate()
    }

    private fun fixTranslation() {
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val bmpW = planBitmap.width * scale
        val bmpH = planBitmap.height * scale
        // Левый/правый край
        if (bmpW <= viewW) {
            translateX = (viewW - bmpW) / 2f
        } else {
            translateX = translateX.coerceIn(viewW - bmpW, 0f)
        }
        // Верх/низ
        if (bmpH <= viewH) {
            translateY = (viewH - bmpH) / 2f
        } else {
            translateY = translateY.coerceIn(viewH - bmpH, 0f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmpW = planBitmap.width.toFloat()
        val bmpH = planBitmap.height.toFloat()
        // --- Инициализация стартового масштаба и позиции ---
        if (!isInitialized && viewW > 0 && viewH > 0) {
            minScale = viewW / bmpW
            scale = minScale
            val imgH = bmpH * scale
            translateX = 0f
            translateY = (viewH - imgH) / 2f
            isInitialized = true
        }
        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(translateX, translateY)
        val saveCount = canvas.save()
        canvas.concat(matrix)
        // Рисуем план
        canvas.drawBitmap(planBitmap, null, RectF(0f, 0f, bmpW, bmpH), null)
        // Затемняем план
        val darkenPaint = Paint().apply { color = Color.argb(70, 0, 0, 0) }
        canvas.drawRect(0f, 0f, bmpW, bmpH, darkenPaint)
        // Маска с белой подсветкой
        val maskHighlight = Bitmap.createBitmap(maskBitmap.width, maskBitmap.height, Bitmap.Config.ARGB_8888)
        val maskPixels = IntArray(maskBitmap.width * maskBitmap.height)
        maskBitmap.getPixels(maskPixels, 0, maskBitmap.width, 0, 0, maskBitmap.width, maskBitmap.height)
        val highlightColor = Color.argb(153, 255, 255, 255) // 60% непрозрачности (60% opacity)
        for (i in maskPixels.indices) {
            if (maskPixels[i] == Color.WHITE) maskPixels[i] = highlightColor else maskPixels[i] = Color.TRANSPARENT
        }
        maskHighlight.setPixels(maskPixels, 0, maskBitmap.width, 0, 0, maskBitmap.width, maskBitmap.height)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(maskHighlight, null, RectF(0f, 0f, bmpW, bmpH), paint)
        paint.xfermode = null
        canvas.restoreToCount(saveCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isPanning = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    isPanning = true
                    lastTouchX = (event.getX(0) + event.getX(1)) / 2f
                    lastTouchY = (event.getY(0) + event.getY(1)) / 2f
                    lastTranslateX = translateX
                    lastTranslateY = translateY
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPanning && event.pointerCount == 2 && !scaleDetector.isInProgress) {
                    val currTouchX = (event.getX(0) + event.getX(1)) / 2f
                    val currTouchY = (event.getY(0) + event.getY(1)) / 2f
                    val dx = currTouchX - lastTouchX
                    val dy = currTouchY - lastTouchY
                    translateX = lastTranslateX + dx
                    translateY = lastTranslateY + dy
                    fixTranslation()
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                isPanning = false
            }
        }
        return true
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val prevScale = scale
            scale *= detector.scaleFactor
            scale = scale.coerceIn(minScale, 5f)
            // Центрируем зум относительно центра между пальцами
            val focusX = detector.focusX
            val focusY = detector.focusY
            val prevImageX = (focusX - translateX) / prevScale
            val prevImageY = (focusY - translateY) / prevScale
            translateX = focusX - prevImageX * scale
            translateY = focusY - prevImageY * scale
            fixTranslation()
            invalidate()
            return true
        }
    })
} 