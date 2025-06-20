package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.Toast
import android.view.View
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import ru.dvfu.diplom3d.api.RetrofitInstance

class UserListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Toolbar
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Список пользователей"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)

        // Scrollable list
        val scrollView = ScrollView(this)
        val scrollParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        scrollParams.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material)
        scrollView.layoutParams = scrollParams
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(32, 48, 32, 32)
        scrollView.addView(content)
        layout.addView(scrollView)

        // --- Полноэкранный ProgressBar ---
        val fullScreenLoading = FrameLayout(this)
        fullScreenLoading.setBackgroundColor(0x80000000.toInt())
        fullScreenLoading.visibility = View.GONE
        fullScreenLoading.isClickable = true
        fullScreenLoading.isFocusable = true
        val progressBar = android.widget.ProgressBar(this)
        val pbParams = FrameLayout.LayoutParams(128, 128)
        pbParams.gravity = android.view.Gravity.CENTER
        progressBar.layoutParams = pbParams
        fullScreenLoading.addView(progressBar)
        val overlayParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        fullScreenLoading.layoutParams = overlayParams
        layout.addView(fullScreenLoading)

        setContentView(layout)
        setSupportActionBar(toolbar)

        // Загрузка пользователей
        fullScreenLoading.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val baseUrl = prefs.getString("server_url", "") ?: ""
            val api = RetrofitInstance.getApiService(baseUrl, this@UserListActivity)
            try {
                val response = api.getUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    for (user in users) {
                        val card = MaterialCardView(this@UserListActivity)
                        val cardParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        cardParams.bottomMargin = 24
                        card.layoutParams = cardParams
                        card.radius = 24f
                        card.cardElevation = 8f
                        card.setContentPadding(32, 32, 32, 32)

                        val cardLayout = LinearLayout(this@UserListActivity)
                        cardLayout.orientation = LinearLayout.VERTICAL
                        card.addView(cardLayout)

                        // Заголовок карточки — display_name
                        val titleView = TextView(this@UserListActivity)
                        titleView.text = user.display_name
                        titleView.textSize = 18f
                        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
                        titleView.setTextColor(0xFF000000.toInt())
                        titleView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                        val titleParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        titleParams.bottomMargin = 12
                        cardLayout.addView(titleView, titleParams)

                        // Вертикальный вывод: каждая пара на новой строке
                        fun makeRow(label: String, value: String): LinearLayout {
                            val row = LinearLayout(this@UserListActivity)
                            row.orientation = LinearLayout.HORIZONTAL
                            val labelView = TextView(this@UserListActivity)
                            labelView.text = label
                            labelView.setTypeface(null, android.graphics.Typeface.BOLD)
                            labelView.textSize = 16f
                            labelView.setTextColor(0xFF444444.toInt())
                            val valueView = TextView(this@UserListActivity)
                            valueView.text = value
                            valueView.textSize = 16f
                            valueView.setTextColor(0xFF444444.toInt())
                            row.addView(labelView)
                            row.addView(valueView)
                            return row
                        }
                        cardLayout.addView(makeRow("ID: ", user.id.toString()))
                        cardLayout.addView(makeRow("Логин: ", user.username))
                        cardLayout.addView(makeRow("Тип: ", when {
                            user.is_superuser -> "Суперпользователь"
                            user.is_staff -> "Сотрудник"
                            else -> "Пользователь"
                        }))

                        card.setOnClickListener {
                            val intent = Intent(this@UserListActivity, UserDetailActivity::class.java)
                            intent.putExtra("user_id", user.id)
                            startActivity(intent)
                        }
                        content.addView(card)
                    }
                } else if (response.code() == 401) {
                    prefs.edit().remove("auth_token").apply()
                    Toast.makeText(this@UserListActivity, "Авторизационные данные утратили актуальность", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@UserListActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@UserListActivity, "Ошибка загрузки пользователей: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserListActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                fullScreenLoading.visibility = View.GONE
            }
        }
    }
} 