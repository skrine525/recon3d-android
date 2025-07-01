package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class ConfigureRoomsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Настройка номеров кабинетов"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)

        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(32, 48, 32, 32)
        val planId = intent.getStringExtra("plan_id")
        val meshId = intent.getStringExtra("mesh_id")
        val infoText = TextView(this)
        infoText.text = "plan_id: $planId\nmesh_id: $meshId"
        infoText.textSize = 18f
        content.addView(infoText)
        layout.addView(content)

        setContentView(layout)
        setSupportActionBar(toolbar)
    }
} 