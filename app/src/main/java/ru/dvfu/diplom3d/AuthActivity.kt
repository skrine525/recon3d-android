package ru.dvfu.diplom3d

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.databinding.ActivityAuthBinding
import ru.dvfu.diplom3d.api.RetrofitInstance
import ru.dvfu.diplom3d.api.RegisterRequest
import ru.dvfu.diplom3d.api.LoginRequest
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.content.Intent

class AuthActivity : FragmentActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        if (!token.isNullOrEmpty()) {
            startActivity(Intent(this, LoadingActivity::class.java))
            finish()
            return
        }

        val baseUrl = prefs.getString("server_url", "") ?: ""
        if (!token.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                val api = RetrofitInstance.getApiService(baseUrl)
                try {
                    val authHeader = "Token $token"
                    val response = api.getMe(authHeader)
                    if (response.isSuccessful) {
                        goToMainMenu()
                        return@launch
                    }
                } catch (_: Exception) {}
            }
        }

        // Показываем форму входа, скрываем регистрацию
        showLoginForm()

        binding.buttonShowRegister.setOnClickListener {
            showRegisterForm()
        }
        binding.buttonShowLogin.setOnClickListener {
            showLoginForm()
        }

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextRegisterLogin.text.toString().trim()
            val password = binding.editTextRegisterPassword.text.toString()
            val rePassword = binding.editTextRegisterPasswordRepeat.text.toString()
            if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = RetrofitInstance.getApiService(baseUrl)
                try {
                    val response = api.register(RegisterRequest(username, password, rePassword))
                    Log.d("AuthTest", "Ответ регистрации: code=${response.code()}, body=${response.body()}, error=${response.errorBody()?.string()}")
                    if (response.code() == 201) {
                        Toast.makeText(this@AuthActivity, "Регистрация успешна! Выполняем вход...", Toast.LENGTH_SHORT).show()
                        // Автоматический логин
                        val loginResp = api.login(LoginRequest(username, password))
                        Log.d("AuthTest", "Авто-логин после регистрации: code=${loginResp.code()}, body=${loginResp.body()}, error=${loginResp.errorBody()?.string()}")
                        if (loginResp.isSuccessful) {
                            val token = loginResp.body()?.auth_token
                            Log.d("AuthTest", "Токен после регистрации: $token")
                            if (!token.isNullOrEmpty()) saveToken(token)
                            Toast.makeText(this@AuthActivity, "Вход выполнен! Токен: $token", Toast.LENGTH_SHORT).show()
                            goToMainMenu()
                        } else {
                            Log.e("AuthTest", "Ошибка авто-логина: code=${loginResp.code()}, error=${loginResp.errorBody()?.string()}")
                            showErrorDialog("Ошибка входа после регистрации: ${loginResp.code()}\n${loginResp.errorBody()?.string()}")
                        }
                    } else {
                        Log.e("AuthTest", "Ошибка регистрации: code=${response.code()}, error=${response.errorBody()?.string()}")
                        showErrorDialog("Ошибка регистрации: ${response.code()}\n${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("AuthTest", "Ошибка сети при регистрации", e)
                    showErrorDialog("Ошибка сети: ${e.message}")
                }
            }
        }

        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextLogin.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = RetrofitInstance.getApiService(baseUrl)
                try {
                    val response = api.login(LoginRequest(username, password))
                    Log.d("AuthTest", "Ответ логина: code=${response.code()}, body=${response.body()}, error=${response.errorBody()?.string()}")
                    if (response.isSuccessful) {
                        val token = response.body()?.auth_token
                        Log.d("AuthTest", "Токен после логина: $token")
                        if (!token.isNullOrEmpty()) saveToken(token)
                        Toast.makeText(this@AuthActivity, "Вход выполнен! Токен: $token", Toast.LENGTH_SHORT).show()
                        goToMainMenu()
                    } else {
                        Log.e("AuthTest", "Ошибка логина: code=${response.code()}, error=${response.errorBody()?.string()}")
                        showErrorDialog("Ошибка входа: ${response.code()}\n${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("AuthTest", "Ошибка сети при логине", e)
                    showErrorDialog("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun showLoginForm() {
        binding.loginForm.visibility = View.VISIBLE
        binding.registerForm.visibility = View.GONE
    }

    private fun showRegisterForm() {
        binding.loginForm.visibility = View.GONE
        binding.registerForm.visibility = View.VISIBLE
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun saveToken(token: String) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putString("auth_token", token)
            .apply()
    }

    private fun goToMainMenu() {
        startActivity(Intent(this, MainMenuActivity::class.java))
        finish()
    }
} 