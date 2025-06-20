package ru.dvfu.diplom3d

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class ServerSetupActivity : ComponentActivity() {
    private lateinit var editText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_setup)
        editText = findViewById(R.id.editTextServerUrl)
        button = findViewById(R.id.buttonConfirm)

        button.setOnClickListener {
            val url = editText.text.toString().trim().removeSuffix("/")
            Log.d("ServerSetup", "Кнопка 'Подтвердить' нажата, введён адрес: $url")
            if (url.isEmpty()) {
                Toast.makeText(this, "Введите адрес сервера", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                if (checkServer(url)) {
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
        Log.d("ServerSetup", "Пробуем подключиться к серверу: $url/api/v1/mobile/meta")
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url("$url/api/v1/common/meta").build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            val success = response.isSuccessful
            Log.d("ServerSetup", "Ответ сервера: код ${response.code}, успешность: $success")
            success
        } catch (e: Exception) {
            Log.e("ServerSetup", "Ошибка при подключении к серверу", e)
            false
        }
    }
} 