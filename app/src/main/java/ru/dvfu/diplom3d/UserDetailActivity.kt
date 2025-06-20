package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.view.Gravity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance
import ru.dvfu.diplom3d.api.UserResponse
import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale

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

        // Основной вертикальный layout внутри ScrollView
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

        setContentView(layout)
        setSupportActionBar(toolbar)

        // --- Загрузка пользователя ---
        fullScreenLoading.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val baseUrl = prefs.getString("server_url", "") ?: ""
            val api = RetrofitInstance.getApiService(baseUrl, this@UserDetailActivity)
            try {
                val response = api.getUser(userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // --- CardView: Учётная запись ---
                        val accountCard = MaterialCardView(this@UserDetailActivity)
                        val accountCardParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountCardParams.topMargin = 0
                        accountCardParams.bottomMargin = 32
                        accountCard.layoutParams = accountCardParams
                        accountCard.radius = 24f
                        accountCard.cardElevation = 8f
                        accountCard.setContentPadding(32, 32, 32, 32)
                        val accountLayout = LinearLayout(this@UserDetailActivity)
                        accountLayout.orientation = LinearLayout.VERTICAL
                        accountLayout.setPadding(0, 0, 0, 0)
                        accountCard.addView(accountLayout)
                        val accountTitle = TextView(this@UserDetailActivity)
                        accountTitle.text = "Учётная запись"
                        accountTitle.textSize = 18f
                        accountTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                        accountTitle.setTextColor(0xFF000000.toInt())
                        accountTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        val accountTitleParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountTitleParams.bottomMargin = 12
                        accountLayout.addView(accountTitle, accountTitleParams)
                        fun makeRow(label: String, value: String): LinearLayout {
                            val row = LinearLayout(this@UserDetailActivity)
                            row.orientation = LinearLayout.HORIZONTAL
                            val labelView = TextView(this@UserDetailActivity)
                            labelView.text = label
                            labelView.setTypeface(null, android.graphics.Typeface.BOLD)
                            labelView.textSize = 16f
                            labelView.setTextColor(0xFF444444.toInt())
                            val valueView = TextView(this@UserDetailActivity)
                            valueView.text = value
                            valueView.textSize = 16f
                            valueView.setTextColor(0xFF444444.toInt())
                            row.addView(labelView)
                            row.addView(valueView)
                            return row
                        }
                        accountLayout.addView(makeRow("ID: ", user.id.toString()))
                        accountLayout.addView(makeRow("Логин: ", user.username))
                        accountLayout.addView(makeRow("Тип: ", when {
                            user.is_superuser -> "Суперпользователь"
                            user.is_staff -> "Сотрудник"
                            else -> "Пользователь"
                        }))
                        val iso = user.date_joined.replace('T', ' ').replace(Regex("\\..*"), "")
                        val formattedDate = try {
                            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                            formatter.format(parser.parse(iso)!!)
                        } catch (e: Exception) {
                            iso
                        }
                        accountLayout.addView(makeRow("Регистрация: ", formattedDate))
                        content.addView(accountCard)
                        // --- CardView: Основная информация ---
                        val infoCard = MaterialCardView(this@UserDetailActivity)
                        val infoCardParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        infoCardParams.bottomMargin = 32
                        infoCard.layoutParams = infoCardParams
                        infoCard.radius = 24f
                        infoCard.cardElevation = 8f
                        infoCard.setContentPadding(32, 32, 32, 32)
                        val infoLayout = LinearLayout(this@UserDetailActivity)
                        infoLayout.orientation = LinearLayout.VERTICAL
                        infoCard.addView(infoLayout)
                        val infoTitle = TextView(this@UserDetailActivity)
                        infoTitle.text = "Персональные данные"
                        infoTitle.textSize = 18f
                        infoTitle.setTextColor(0xFF000000.toInt())
                        infoTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        val infoTitleParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        infoTitleParams.bottomMargin = 12
                        infoLayout.addView(infoTitle, infoTitleParams)
                        infoLayout.addView(makeRow("Имя: ", user.first_name))
                        infoLayout.addView(makeRow("Фамилия: ", user.last_name))
                        infoLayout.addView(makeRow("Email: ", user.email))
                        content.addView(infoCard)
                    }
                } else {
                    Toast.makeText(this@UserDetailActivity, "Ошибка загрузки пользователя: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserDetailActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                fullScreenLoading.visibility = View.GONE
            }
        }
    }
} 