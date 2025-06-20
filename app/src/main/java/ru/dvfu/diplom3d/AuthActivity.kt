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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class AuthActivity : FragmentActivity() {
    private lateinit var binding: ActivityAuthBinding

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
        title.text = "Вход"
        title.textSize = 18f
        title.setTextColor(0xFF000000.toInt())
        title.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val titleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        titleParams.bottomMargin = 12
        cardLayout.addView(title, titleParams)

        // --- Login form ---
        val loginForm = LinearLayout(this)
        loginForm.orientation = LinearLayout.VERTICAL
        loginForm.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        cardLayout.addView(loginForm)

        val loginInputLayout = TextInputLayout(this)
        loginInputLayout.hint = "Логин"
        loginInputLayout.boxBackgroundMode = 0
        val loginInput = TextInputEditText(this)
        loginInputLayout.addView(loginInput)
        loginForm.addView(loginInputLayout)

        val passwordInputLayout = TextInputLayout(this)
        passwordInputLayout.hint = "Пароль"
        passwordInputLayout.boxBackgroundMode = 0
        val passwordInput = TextInputEditText(this)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordInputLayout.addView(passwordInput)
        loginForm.addView(passwordInputLayout)

        val loginBtn = Button(this)
        loginBtn.text = "Войти"
        loginBtn.setBackgroundResource(R.drawable.blue_button)
        loginBtn.setTextColor(0xFFFFFFFF.toInt())
        val loginBtnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        loginBtnParams.topMargin = 16
        loginBtn.layoutParams = loginBtnParams
        loginForm.addView(loginBtn)

        val showRegister = TextView(this)
        showRegister.text = "Зарегистрироваться"
        showRegister.setTextColor(0xFF1976D2.toInt())
        showRegister.textSize = 16f
        showRegister.textAlignment = View.TEXT_ALIGNMENT_CENTER
        showRegister.setPadding(0, 16, 0, 0)
        showRegister.isClickable = true
        showRegister.isFocusable = true
        loginForm.addView(showRegister)

        // --- Register form ---
        val registerForm = LinearLayout(this)
        registerForm.orientation = LinearLayout.VERTICAL
        registerForm.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        registerForm.visibility = View.GONE
        cardLayout.addView(registerForm)

        val regLoginInputLayout = TextInputLayout(this)
        regLoginInputLayout.hint = "Логин"
        regLoginInputLayout.boxBackgroundMode = 0
        val regLoginInput = TextInputEditText(this)
        regLoginInputLayout.addView(regLoginInput)
        registerForm.addView(regLoginInputLayout)

        val regPasswordInputLayout = TextInputLayout(this)
        regPasswordInputLayout.hint = "Пароль"
        regPasswordInputLayout.boxBackgroundMode = 0
        val regPasswordInput = TextInputEditText(this)
        regPasswordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        regPasswordInputLayout.addView(regPasswordInput)
        registerForm.addView(regPasswordInputLayout)

        val regRepeatPasswordInputLayout = TextInputLayout(this)
        regRepeatPasswordInputLayout.hint = "Повторите пароль"
        regRepeatPasswordInputLayout.boxBackgroundMode = 0
        val regRepeatPasswordInput = TextInputEditText(this)
        regRepeatPasswordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        regRepeatPasswordInputLayout.addView(regRepeatPasswordInput)
        registerForm.addView(regRepeatPasswordInputLayout)

        val registerBtn = Button(this)
        registerBtn.text = "Зарегистрироваться"
        registerBtn.setBackgroundResource(R.drawable.blue_button)
        registerBtn.setTextColor(0xFFFFFFFF.toInt())
        val registerBtnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        registerBtnParams.topMargin = 16
        registerBtn.layoutParams = registerBtnParams
        registerForm.addView(registerBtn)

        val showLogin = TextView(this)
        showLogin.text = "Войти"
        showLogin.setTextColor(0xFF1976D2.toInt())
        showLogin.textSize = 16f
        showLogin.textAlignment = View.TEXT_ALIGNMENT_CENTER
        showLogin.setPadding(0, 16, 0, 0)
        showLogin.isClickable = true
        showLogin.isFocusable = true
        registerForm.addView(showLogin)

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

        // --- Переключение форм ---
        showRegister.setOnClickListener {
            loginForm.visibility = View.GONE
            registerForm.visibility = View.VISIBLE
            title.text = "Регистрация"
        }
        showLogin.setOnClickListener {
            loginForm.visibility = View.VISIBLE
            registerForm.visibility = View.GONE
            title.text = "Вход"
        }

        // --- Логика ---
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val baseUrl = prefs.getString("server_url", "") ?: ""

        loginBtn.setOnClickListener {
            val username = loginInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Заполните все поля", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fullScreenLoading.visibility = View.VISIBLE
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val api = RetrofitInstance.getApiService(baseUrl, this@AuthActivity)
                try {
                    val response = api.login(ru.dvfu.diplom3d.api.LoginRequest(username, password))
                    if (response.isSuccessful) {
                        val token = response.body()?.auth_token
                        if (!token.isNullOrEmpty()) saveToken(token)
                        android.widget.Toast.makeText(this@AuthActivity, "Вход выполнен!", android.widget.Toast.LENGTH_SHORT).show()
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

        registerBtn.setOnClickListener {
            val username = regLoginInput.text.toString().trim()
            val password = regPasswordInput.text.toString()
            val rePassword = regRepeatPasswordInput.text.toString()
            if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                android.widget.Toast.makeText(this, "Заполните все поля", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fullScreenLoading.visibility = View.VISIBLE
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val api = RetrofitInstance.getApiService(baseUrl, this@AuthActivity)
                try {
                    val response = api.register(ru.dvfu.diplom3d.api.RegisterRequest(username, password, rePassword))
                    if (response.code() == 201) {
                        android.widget.Toast.makeText(this@AuthActivity, "Регистрация успешна! Выполняем вход...", android.widget.Toast.LENGTH_SHORT).show()
                        val loginResp = api.login(ru.dvfu.diplom3d.api.LoginRequest(username, password))
                        if (loginResp.isSuccessful) {
                            val token = loginResp.body()?.auth_token
                            if (!token.isNullOrEmpty()) saveToken(token)
                            android.widget.Toast.makeText(this@AuthActivity, "Вход выполнен!", android.widget.Toast.LENGTH_SHORT).show()
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