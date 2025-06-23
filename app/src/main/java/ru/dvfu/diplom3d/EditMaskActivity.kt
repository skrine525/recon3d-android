package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Button
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide

class EditMaskActivity : AppCompatActivity() {
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

        val imageView = ImageView(this)
        val imageParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageParams.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material)
        imageView.layoutParams = imageParams
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        layout.addView(imageView)

        val saveBtn = Button(this)
        saveBtn.text = "Сохранить"
        val saveBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        saveBtnParams.topMargin = 32
        saveBtnParams.leftMargin = 32
        saveBtnParams.rightMargin = 32
        saveBtnParams.bottomMargin = 32
        saveBtnParams.gravity = android.view.Gravity.BOTTOM
        saveBtn.layoutParams = saveBtnParams
        layout.addView(saveBtn)

        setContentView(layout)

        val maskUrl = intent.getStringExtra("mask_url")
        if (!maskUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(maskUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)
        } else {
            Toast.makeText(this, "Не удалось загрузить маску", Toast.LENGTH_SHORT).show()
        }

        saveBtn.setOnClickListener {
            Toast.makeText(this, "Сохранение пока не реализовано", Toast.LENGTH_SHORT).show()
        }
    }
} 