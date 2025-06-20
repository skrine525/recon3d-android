package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.view.Gravity
import android.widget.LinearLayout
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance

class AuthLoadingActivity : AppCompatActivity() {
    companion object {
        // Временное хранилище для данных пользователя (очищается при перезапуске приложения)
        var userMe: ru.dvfu.diplom3d.api.UserMeResponse? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // Убираем заголовок
        supportRequestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        val textView = TextView(this)
        textView.text = "Загрузка"
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
        val progressBar = android.widget.ProgressBar(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = 32
        progressBar.layoutParams = params
        layout.addView(textView)
        layout.addView(progressBar)
        setContentView(layout)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        val baseUrl = prefs.getString("server_url", "") ?: ""
        if (!token.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                val api = RetrofitInstance.getApiService(baseUrl)
                try {
                    val authHeader = "Token $token"
                    val response = api.getMe(authHeader)
                    if (response.isSuccessful) {
                        val user = response.body()
                        userMe = user // сохраняем во временное хранилище
                        if (user?.is_staff == true) {
                            startActivity(Intent(this@AuthLoadingActivity, StaffMainMenuActivity::class.java))
                        } else {
                            startActivity(Intent(this@AuthLoadingActivity, UserMainMenuActivity::class.java))
                        }
                        finish()
                    } else {
                        startActivity(Intent(this@AuthLoadingActivity, AuthActivity::class.java))
                        finish()
                    }
                } catch (_: Exception) {
                    startActivity(Intent(this@AuthLoadingActivity, AuthActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
} 