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
import android.widget.Button
import android.widget.SeekBar
import android.widget.LinearLayout
import android.widget.ToggleButton
import java.util.Stack
import android.widget.TextView
import android.view.View
import android.content.Intent
import java.io.FileOutputStream

class EditMaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val planPath = intent.getStringExtra("plan_path")
        val maskPath = intent.getStringExtra("mask_path")
        val planBitmap = BitmapFactory.decodeFile(planPath)
        val maskBitmapOrig = BitmapFactory.decodeFile(maskPath)
        val maskBitmap = maskBitmapOrig.copy(Bitmap.Config.ARGB_8888, true)
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
        // --- Панель управления ---
        val controls = LinearLayout(this)
        controls.orientation = LinearLayout.HORIZONTAL
        controls.setPadding(32, 16, 32, 16)
        controls.setBackgroundColor(0xAAFFFFFF.toInt())
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.BOTTOM
        controls.layoutParams = params
        // Кнопка режим кисти
        val toggleMode = ToggleButton(this)
        toggleMode.textOn = "Удалять"
        toggleMode.textOff = "Добавлять"
        toggleMode.text = "Добавлять"
        toggleMode.setBackgroundResource(R.drawable.blue_button)
        toggleMode.setTextColor(0xFFFFFFFF.toInt())
        val toggleParams = LinearLayout.LayoutParams(0, 120, 1f) // фиксированная высота
        toggleParams.marginEnd = 16
        toggleMode.layoutParams = toggleParams
        controls.addView(toggleMode)
        // Undo
        val undoBtn = Button(this)
        undoBtn.text = "Отменить"
        undoBtn.setBackgroundResource(R.drawable.red_button)
        undoBtn.setTextColor(0xFFFFFFFF.toInt())
        val undoParams = LinearLayout.LayoutParams(0, 120, 1f) // фиксированная высота
        undoParams.marginEnd = 16
        undoBtn.layoutParams = undoParams
        controls.addView(undoBtn)
        // Слайдер толщины с подписью
        val brushLayout = LinearLayout(this)
        brushLayout.orientation = LinearLayout.VERTICAL
        val brushLabel = TextView(this)
        brushLabel.text = "Толщина кисти"
        brushLabel.textSize = 14f
        brushLabel.setTextColor(0xFF000000.toInt())
        brushLabel.textAlignment = View.TEXT_ALIGNMENT_CENTER
        brushLayout.addView(brushLabel)
        val seekBar = SeekBar(this)
        seekBar.max = 100
        seekBar.progress = 32
        seekBar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        brushLayout.addView(seekBar)
        val brushParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        brushLayout.layoutParams = brushParams
        controls.addView(brushLayout)
        layout.addView(controls)
        // Кнопка сохранить — отдельным блоком ниже
        val saveBtn = Button(this)
        saveBtn.text = "Сохранить"
        saveBtn.setBackgroundResource(R.drawable.green_button)
        saveBtn.setTextColor(0xFFFFFFFF.toInt())
        val saveParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        saveParams.gravity = android.view.Gravity.BOTTOM
        saveParams.setMargins(64, 0, 64, 32)
        saveBtn.layoutParams = saveParams
        // Сдвигаем кнопку выше нижней панели
        saveBtn.translationY = -180f
        layout.addView(saveBtn)
        setContentView(layout)
        // --- Логика ---
        toggleMode.setOnCheckedChangeListener { _, isChecked ->
            maskEditView.setEraseMode(isChecked)
        }
        undoBtn.setOnClickListener {
            maskEditView.undo()
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maskEditView.setBrushSize(progress.coerceAtLeast(4).toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        saveBtn.setOnClickListener {
            // Сохраняем maskBitmap в файл
            val file = File(filesDir, "mask_saved_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            maskBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            val resultIntent = Intent()
            resultIntent.putExtra("saved_mask_path", file.absolutePath)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
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
    private var isDrawing = false
    private var lastDrawX = 0f
    private var lastDrawY = 0f
    private var isEraseMode = false
    private var brushSize = 32f
    private val undoStack = Stack<Bitmap>()
    private val drawPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = brushSize
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    private val maskCanvas = Canvas(maskBitmap)

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
        // Рисование одним пальцем
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDrawing = true
                    val (x, y) = screenToMask(event.x, event.y)
                    lastDrawX = x
                    lastDrawY = y
                    // Сохраняем в undo стек
                    val snap = maskBitmap.copy(Bitmap.Config.ARGB_8888, true)
                    undoStack.push(snap)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDrawing) {
                        val (x, y) = screenToMask(event.x, event.y)
                        maskCanvas.drawLine(lastDrawX, lastDrawY, x, y, drawPaint)
                        lastDrawX = x
                        lastDrawY = y
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDrawing = false
                }
            }
            return true
        }
        // Зум и пан двумя пальцами
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

    // Переводит координаты экрана в координаты маски
    private fun screenToMask(screenX: Float, screenY: Float): Pair<Float, Float> {
        val bmpW = maskBitmap.width.toFloat()
        val bmpH = maskBitmap.height.toFloat()
        val scaleX = scale
        val scaleY = scale
        val x = (screenX - translateX) / scaleX
        val y = (screenY - translateY) / scaleY
        return Pair(x.coerceIn(0f, bmpW - 1), y.coerceIn(0f, bmpH - 1))
    }

    fun setEraseMode(erase: Boolean) {
        isEraseMode = erase
        drawPaint.color = if (erase) Color.BLACK else Color.WHITE
    }

    fun setBrushSize(size: Float) {
        brushSize = size
        drawPaint.strokeWidth = brushSize
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.pop()
            val c = Canvas(maskBitmap)
            c.drawBitmap(prev, 0f, 0f, null)
            invalidate()
        }
    }
} 