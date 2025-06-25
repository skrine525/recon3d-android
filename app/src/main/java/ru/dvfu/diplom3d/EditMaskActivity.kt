package ru.dvfu.diplom3d

import android.content.Context
import android.graphics.*
import android.os.Bundle
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
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // FIT_CENTER
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val bmpW = planBitmap.width.toFloat()
        val bmpH = planBitmap.height.toFloat()
        val scale = minOf(viewW / bmpW, viewH / bmpH)
        val outW = bmpW * scale
        val outH = bmpH * scale
        val left = (viewW - outW) / 2f
        val top = (viewH - outH) / 2f
        val fitRect = RectF(left, top, left + outW, top + outH)
        // Рисуем план
        canvas.drawBitmap(planBitmap, null, fitRect, null)
        // Создаём маску с белой подсветкой только на белых участках
        val maskHighlight = Bitmap.createBitmap(maskBitmap.width, maskBitmap.height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskHighlight)
        val highlightPaint = Paint().apply { color = Color.WHITE; alpha = 180 }
        val maskPixels = IntArray(maskBitmap.width * maskBitmap.height)
        maskBitmap.getPixels(maskPixels, 0, maskBitmap.width, 0, 0, maskBitmap.width, maskBitmap.height)
        for (i in maskPixels.indices) {
            if (maskPixels[i] == Color.WHITE) maskPixels[i] = Color.WHITE else maskPixels[i] = Color.TRANSPARENT
        }
        maskHighlight.setPixels(maskPixels, 0, maskBitmap.width, 0, 0, maskBitmap.width, maskBitmap.height)
        // Наложить белую маску на план
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(maskHighlight, null, fitRect, highlightPaint)
        paint.xfermode = null
    }
} 