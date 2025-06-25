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

class EditMaskActivity : AppCompatActivity() {
    private lateinit var photoEditorView: PhotoEditorView
    private lateinit var maskOverlayView: ImageView // overlay для маски
    private lateinit var photoEditor: PhotoEditor
    private lateinit var btnBrush: Button
    private lateinit var btnEraser: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Редактирование маски"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)

        // PhotoEditorView (фон — план, overlay — маска)
        photoEditorView = PhotoEditorView(this)
        val photoEditorParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        photoEditorParams.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material)
        photoEditorView.layoutParams = photoEditorParams
        layout.addView(photoEditorView)

        // Overlay для маски
        maskOverlayView = ImageView(this)
        maskOverlayView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        maskOverlayView.scaleType = ImageView.ScaleType.FIT_CENTER
        maskOverlayView.alpha = 0.7f
        layout.addView(maskOverlayView)

        // Панель кнопок
        val buttonLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        val buttonLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayoutParams.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material) + 16
        buttonLayout.layoutParams = buttonLayoutParams
        buttonLayout.setPadding(16, 16, 16, 16)
        buttonLayout.setBackgroundColor(0x66FFFFFF)
        layout.addView(buttonLayout)

        btnBrush = Button(this)
        btnBrush.text = "Кисть"
        buttonLayout.addView(btnBrush)
        btnEraser = Button(this)
        btnEraser.text = "Ластик"
        buttonLayout.addView(btnEraser)

        btnSave = Button(this)
        btnSave.text = "Сохранить"
        val saveBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        saveBtnParams.leftMargin = 32
        saveBtnParams.rightMargin = 32
        saveBtnParams.bottomMargin = 32
        saveBtnParams.gravity = android.view.Gravity.BOTTOM
        btnSave.layoutParams = saveBtnParams
        layout.addView(btnSave)

        btnCancel = Button(this)
        btnCancel.text = "Отмена"
        val cancelBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        cancelBtnParams.leftMargin = 32
        cancelBtnParams.rightMargin = 32
        cancelBtnParams.bottomMargin = 120
        cancelBtnParams.gravity = android.view.Gravity.BOTTOM
        btnCancel.layoutParams = cancelBtnParams
        layout.addView(btnCancel)

        setContentView(layout)

        val maskUrl = intent.getStringExtra("mask_url")
        val planUrl = intent.getStringExtra("plan_url")

        // Загружаем план как фон
        if (!planUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(planUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.drawable.ic_menu_report_image)
                .into(photoEditorView.source)
            // Затемняем фон
            photoEditorView.source.setColorFilter(Color.argb(120, 0, 0, 0))
        }
        // Загружаем маску как overlay
        if (!maskUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(maskUrl)
                .placeholder(android.R.color.transparent)
                .error(android.R.drawable.ic_menu_report_image)
                .into(maskOverlayView)
            maskOverlayView.alpha = 0.7f
        }

        // Инициализация PhotoEditor
        photoEditor = PhotoEditor.Builder(this, photoEditorView)
            .setPinchTextScalable(false)
            .build()
        photoEditor.setBrushDrawingMode(true)
        photoEditor.brushColor = Color.WHITE
        photoEditor.brushSize = 20f

        btnBrush.setOnClickListener {
            photoEditor.setBrushDrawingMode(true)
            photoEditor.brushColor = Color.WHITE
        }
        btnEraser.setOnClickListener {
            photoEditor.setBrushDrawingMode(true)
            photoEditor.brushEraser()
        }
        btnSave.setOnClickListener {
            Toast.makeText(this, "Сохранение пока не реализовано", Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener {
            finish()
        }
    }
} 