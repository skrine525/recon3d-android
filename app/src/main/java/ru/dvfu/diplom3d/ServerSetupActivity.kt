package ru.dvfu.diplom3d

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.dvfu.diplom3d.api.RetrofitInstance
import ru.dvfu.diplom3d.api.ApiService
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class ServerSetupActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var button: Button
    private lateinit var fullScreenLoading: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Корневой layout
        val layout = FrameLayout(this)
        // Основной вертикальный layout
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        val contentParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        content.layoutParams = contentParams
        content.setPadding(32, 48, 32, 32)
        content.gravity = android.view.Gravity.CENTER_VERTICAL
        layout.addView(content)

        // --- CardView ---
        val card = CardView(this)
        val cardParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        cardParams.bottomMargin = 32
        card.layoutParams = cardParams
        card.radius = 24f
        card.cardElevation = 8f
        card.setContentPadding(32, 32, 32, 32)

        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL
        card.addView(cardLayout)

        val title = TextView(this)
        title.text = "Адрес сервера"
        title.textSize = 18f
        title.setTextColor(0xFF000000.toInt())
        title.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val titleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        titleParams.bottomMargin = 12
        cardLayout.addView(title, titleParams)

        val serverInputLayout = TextInputLayout(this)
        serverInputLayout.hint = "Адрес сервера"
        serverInputLayout.boxBackgroundMode = 0
        val serverInput = TextInputEditText(this)
        serverInput.setText("https://dev.radabot.ru")
        serverInputLayout.addView(serverInput)
        cardLayout.addView(serverInputLayout)

        val confirmBtn = Button(this)
        confirmBtn.text = "Подтвердить"
        confirmBtn.setBackgroundResource(R.drawable.blue_button)
        confirmBtn.setTextColor(0xFFFFFFFF.toInt())
        val confirmParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        confirmParams.topMargin = 16
        confirmBtn.layoutParams = confirmParams
        cardLayout.addView(confirmBtn)

        content.addView(card)

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

        confirmBtn.setOnClickListener {
            val url = serverInput.text.toString().trim().removeSuffix("/")
            if (url.isEmpty()) {
                android.widget.Toast.makeText(this, "Введите адрес сервера", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            confirmBtn.isClickable = false
            fullScreenLoading.visibility = View.VISIBLE
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val result = checkServer(url)
                confirmBtn.isClickable = true
                fullScreenLoading.visibility = View.GONE
                if (result) {
                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putString("server_url", url).apply()
                    startActivity(android.content.Intent(this@ServerSetupActivity, AuthActivity::class.java))
                    finish()
                } else {
                    android.widget.Toast.makeText(this@ServerSetupActivity, "Сервер недоступен", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun checkServer(url: String): Boolean {
        Log.d("ServerSetup", "Пробуем подключиться к серверу: $url/api/v1/mobile/meta (Retrofit)")
        return try {
            val api = RetrofitInstance.getApiService(url, this@ServerSetupActivity)
            val response = api.checkServer()
            val success = response.isSuccessful
            Log.d("ServerSetup", "Ответ сервера (Retrofit): код ${response.code()}, успешность: $success")
            success
        } catch (e: Exception) {
            Log.e("ServerSetup", "Ошибка при подключении к серверу (Retrofit)", e)
            false
        }
    }
} 