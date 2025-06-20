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
            startActivity(Intent(this, AuthLoadingActivity::class.java))
            finish()
            return
        }

        val baseUrl = prefs.getString("server_url", "") ?: ""

        // Показываем форму входа, скрываем регистрацию
        showLoginForm()

        binding.buttonShowRegister.setOnClickListener {
            showRegisterForm()
        }
        binding.buttonShowLogin.setOnClickListener {
            showLoginForm()
        }

        val progressBarLogin = binding.root.findViewById<android.widget.ProgressBar>(R.id.progressBarLogin)
        val progressBarRegister = binding.root.findViewById<android.widget.ProgressBar>(R.id.progressBarRegister)
        val fullScreenLoading = findViewById<View>(R.id.fullScreenLoading)

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextRegisterLogin.text.toString().trim()
            val password = binding.editTextRegisterPassword.text.toString()
            val rePassword = binding.editTextRegisterPasswordRepeat.text.toString()
            if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fullScreenLoading.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = RetrofitInstance.getApiService(baseUrl, this@AuthActivity)
                try {
                    val response = api.register(RegisterRequest(username, password, rePassword))
                    if (response.code() == 201) {
                        Toast.makeText(this@AuthActivity, "Регистрация успешна! Выполняем вход...", Toast.LENGTH_SHORT).show()
                        // Автоматический логин
                        val loginResp = api.login(LoginRequest(username, password))
                        if (loginResp.isSuccessful) {
                            val token = loginResp.body()?.auth_token
                            if (!token.isNullOrEmpty()) saveToken(token)
                            Toast.makeText(this@AuthActivity, "Вход выполнен!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@AuthActivity, AuthLoadingActivity::class.java))
                            finish()
                        } else {
                            showErrorDialog("Ошибка входа после регистрации: ${loginResp.code()}\n${loginResp.errorBody()?.string()}")
                        }
                    } else {
                        showErrorDialog("Ошибка регистрации: ${response.code()}\n${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    showErrorDialog("Ошибка сети: ${e.message}")
                } finally {
                    fullScreenLoading.visibility = View.GONE
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
            fullScreenLoading.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = RetrofitInstance.getApiService(baseUrl, this@AuthActivity)
                try {
                    val response = api.login(LoginRequest(username, password))
                    if (response.isSuccessful) {
                        val token = response.body()?.auth_token
                        if (!token.isNullOrEmpty()) saveToken(token)
                        Toast.makeText(this@AuthActivity, "Вход выполнен!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AuthActivity, AuthLoadingActivity::class.java))
                        finish()
                    } else {
                        showErrorDialog("Ошибка входа: ${response.code()}\n${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    showErrorDialog("Ошибка сети: ${e.message}")
                } finally {
                    fullScreenLoading.visibility = View.GONE
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
} 