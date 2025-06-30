package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.TextView

class IdentifyUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Идентификация пользователя"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)
        val id = intent.getIntExtra("reconstruction_id", -1)
        val text = TextView(this)
        text.text = "Здесь будет идентификация пользователя\nID реконструкции: $id"
        text.textSize = 20f
        text.setPadding(32, 200, 32, 32)
        layout.addView(text)
        setContentView(layout)
        setSupportActionBar(toolbar)
    }
} 