package ru.dvfu.diplom3d

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.LinearLayout
import android.view.View

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

        // Карточка с кнопками
        val card = com.google.android.material.card.MaterialCardView(this)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.topMargin = 32
        card.layoutParams = cardParams
        card.radius = 20f
        card.cardElevation = 6f
        card.setContentPadding(32, 32, 32, 32)
        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL

        val cardTitle = TextView(this)
        cardTitle.text = "Визуализация"
        cardTitle.textSize = 20f
        cardTitle.setTextColor(0xFF000000.toInt())
        cardTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        cardLayout.addView(cardTitle)
        val space = View(this)
        space.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 24)
        cardLayout.addView(space)

        val btnView = android.widget.Button(this)
        btnView.text = "Просмотреть"
        btnView.setBackgroundResource(R.drawable.blue_button)
        btnView.setTextColor(0xFFFFFFFF.toInt())
        btnView.setOnClickListener {
            val intent = android.content.Intent(this, ViewMeshActivity::class.java)
            intent.putExtra("mesh_id", id.toString())
            startActivity(intent)
        }
        cardLayout.addView(btnView)

        val btnIdentify = android.widget.Button(this)
        btnIdentify.text = "Идентифицировать пользователя"
        btnIdentify.setBackgroundResource(R.drawable.green_button)
        btnIdentify.setTextColor(0xFFFFFFFF.toInt())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 16
        btnIdentify.layoutParams = params
        btnIdentify.setOnClickListener {
            android.widget.Toast.makeText(this, "Идентификация пользователя (заглушка)", android.widget.Toast.LENGTH_SHORT).show()
        }
        cardLayout.addView(btnIdentify)

        card.addView(cardLayout)
        layout.addView(card)
        setContentView(layout)
    }
} 