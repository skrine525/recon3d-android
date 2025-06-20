package ru.dvfu.diplom3d

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.dvfu.diplom3d.api.RetrofitInstance
import ru.dvfu.diplom3d.api.ApiService

class ServerSetupActivity : ComponentActivity() {
    private lateinit var editText: EditText
    private lateinit var button: Button
    private lateinit var fullScreenLoading: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_setup)
        editText = findViewById(R.id.editTextServerUrl)
        button = findViewById(R.id.buttonConfirm)
        fullScreenLoading = findViewById(R.id.fullScreenLoading)

        button.setOnClickListener {
            val url = editText.text.toString().trim().removeSuffix("/")
            Log.d("ServerSetup", "Кнопка 'Подтвердить' нажата, введён адрес: $url")
            if (url.isEmpty()) {
                Toast.makeText(this, "Введите адрес сервера", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            button.isClickable = false
            fullScreenLoading.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val result = checkServer(url)
                button.isClickable = true
                button.text = "Подтвердить"
                fullScreenLoading.visibility = View.GONE
                if (result) {
                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putString("server_url", url).apply()
                    startActivity(Intent(this@ServerSetupActivity, AuthActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@ServerSetupActivity, "Сервер недоступен", Toast.LENGTH_SHORT).show()
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