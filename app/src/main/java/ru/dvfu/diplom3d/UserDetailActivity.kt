package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.view.Gravity

class UserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("user_id", -1)

        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Пользователь #$userId"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)

        val textView = TextView(this)
        textView.text = "ID пользователя: $userId"
        textView.textSize = 28f
        textView.gravity = Gravity.CENTER
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material)
        textView.layoutParams = params
        layout.addView(textView)

        setContentView(layout)
        setSupportActionBar(toolbar)
    }
} 