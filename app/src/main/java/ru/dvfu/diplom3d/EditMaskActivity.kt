package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.Button
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.ImageView
import android.graphics.Color
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import com.bumptech.glide.Glide
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.max
import kotlin.math.min
import java.util.Stack
import android.graphics.BitmapFactory

class MaskEditView(context: Context, planBitmapSrc: Bitmap, maskBitmapSrc: Bitmap) : View(context) {
    // Приводим маску к размеру плана
    private val planBitmap: Bitmap = planBitmapSrc
    private val maskBitmap: Bitmap = if (maskBitmapSrc.width != planBitmapSrc.width || maskBitmapSrc.height != planBitmapSrc.height) {
        Bitmap.createScaledBitmap(maskBitmapSrc, planBitmapSrc.width, planBitmapSrc.height, true)
    } else maskBitmapSrc

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 40f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private val erasePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 40f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private var currentPath: Path? = null
    private var isErasing = false
    private val pathStack = Stack<Pair<Path, Boolean>>()
    private val redoStack = Stack<Pair<Path, Boolean>>()
    private var matrix = Matrix()
    private var inverseMatrix = Matrix()
    private var scaleFactor = 1f
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var posX = 0f
    private var posY = 0f
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor, 5.0f))
            updateMatrix()
            invalidate()
            return true
        }
    })
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = -1
    private var isPanning = false

    private fun updateMatrix() {
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val bmpW = planBitmap.width.toFloat()
        val bmpH = planBitmap.height.toFloat()
        val scale = min(viewW / bmpW, viewH / bmpH) * scaleFactor
        val outW = bmpW * scale
        val outH = bmpH * scale
        val left = (viewW - outW) / 2f + posX
        val top = (viewH - outH) / 2f + posY
        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(left, top)
        matrix.invert(inverseMatrix)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMatrix()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.concat(matrix)
        // Рисуем план и маску строго в (0,0,bitmap.width,bitmap.height)
        canvas.drawBitmap(planBitmap, 0f, 0f, null)
        val maskPaint = Paint().apply { alpha = 180 }
        canvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)
        // Рисуем пользовательские линии
        for ((path, erase) in pathStack) {
            canvas.drawPath(path, if (erase) erasePaint else paint)
        }
        currentPath?.let {
            canvas.drawPath(it, if (isErasing) erasePaint else paint)
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1 && !scaleDetector.isInProgress) {
                    val pts = floatArrayOf(event.x, event.y)
                    inverseMatrix.mapPoints(pts)
                    currentPath = Path().apply { moveTo(pts[0], pts[1]) }
                    lastTouchX = event.x
                    lastTouchY = event.y
                    activePointerId = event.getPointerId(0)
                    isPanning = false
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                isPanning = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (scaleDetector.isInProgress || isPanning) {
                    if (event.pointerCount >= 2) {
                        val focusX = (event.getX(0) + event.getX(1)) / 2
                        val focusY = (event.getY(0) + event.getY(1)) / 2
                        if (lastFocusX != 0f && lastFocusY != 0f) {
                            posX += focusX - lastFocusX
                            posY += focusY - lastFocusY
                            updateMatrix()
                        }
                        lastFocusX = focusX
                        lastFocusY = focusY
                        invalidate()
                    }
                } else if (activePointerId != -1) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)
                    val pts = floatArrayOf(x, y)
                    inverseMatrix.mapPoints(pts)
                    currentPath?.lineTo(pts[0], pts[1])
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (currentPath != null) {
                    pathStack.push(Pair(currentPath!!, isErasing))
                    currentPath = null
                }
                activePointerId = -1
                lastFocusX = 0f
                lastFocusY = 0f
                isPanning = false
                invalidate()
            }
        }
        return true
    }

    fun setEraseMode(erase: Boolean) {
        isErasing = erase
    }
    fun undo() {
        if (pathStack.isNotEmpty()) {
            redoStack.push(pathStack.pop())
            invalidate()
        }
    }
    fun redo() {
        if (redoStack.isNotEmpty()) {
            pathStack.push(redoStack.pop())
            invalidate()
        }
    }
}

class EditMaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем пути к локальным файлам
        val maskPath = intent.getStringExtra("mask_path")
        val planUrl = intent.getStringExtra("plan_url")
        // Загружаем bitmap плана (фон)
        val planBitmap = if (!planUrl.isNullOrEmpty()) {
            try {
                val input = java.net.URL(planUrl).openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            }
        } else {
            Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        }
        // Загружаем bitmap маски
        val maskBitmap = if (!maskPath.isNullOrEmpty()) {
            BitmapFactory.decodeFile(maskPath)
        } else {
            Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        }
        val maskEditView = MaskEditView(this, planBitmap, maskBitmap)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Редактирование маски"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)
        layout.addView(maskEditView)
        // Кнопки 'Отменить' и 'Сохранить' под Toolbar
        val buttonRow = LinearLayout(this)
        buttonRow.orientation = LinearLayout.HORIZONTAL
        val buttonRowParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, // 100% ширины
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        buttonRowParams.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material) + 32
        buttonRowParams.leftMargin = 32
        buttonRowParams.rightMargin = 32
        buttonRow.layoutParams = buttonRowParams

        val btnCancel = Button(this)
        btnCancel.text = "Отменить"
        btnCancel.setBackgroundResource(R.drawable.red_button)
        btnCancel.setTextColor(0xFFFFFFFF.toInt())
        val btnCancelParams = LinearLayout.LayoutParams(
            0, // 50% ширины
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        btnCancel.layoutParams = btnCancelParams
        btnCancel.setOnClickListener { maskEditView.undo() }
        buttonRow.addView(btnCancel)

        val btnSave = Button(this)
        btnSave.text = "Сохранить"
        btnSave.setBackgroundResource(R.drawable.green_button)
        btnSave.setTextColor(0xFFFFFFFF.toInt())
        val btnSaveParams = LinearLayout.LayoutParams(
            0, // 50% ширины
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        btnSaveParams.marginStart = 16 // небольшой отступ между кнопками
        btnSave.layoutParams = btnSaveParams
        btnSave.setOnClickListener {
            // TODO: реализовать сохранение маски
        }
        buttonRow.addView(btnSave)

        layout.addView(buttonRow)
        setContentView(layout)
    }
} 