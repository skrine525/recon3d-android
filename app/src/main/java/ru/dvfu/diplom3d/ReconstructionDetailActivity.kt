package ru.dvfu.diplom3d

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.LinearLayout

class ReconstructionDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val toolbar = Toolbar(this)
        toolbar.title = "Детали реконструкции"
        layout.addView(toolbar)
        setSupportActionBar(toolbar)
        val id = intent.getIntExtra("reconstruction_id", -1)
        val tv = TextView(this)
        tv.text = "ID реконструкции: $id"
        tv.textSize = 20f
        layout.addView(tv)
        setContentView(layout)
    }
} 