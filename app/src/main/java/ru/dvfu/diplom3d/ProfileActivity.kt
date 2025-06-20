package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.view.ViewGroup
import android.view.View
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance
import org.json.JSONObject
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Toolbar
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Профиль"
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

        // Основной вертикальный layout
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        val contentParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        contentParams.topMargin = resources.getDimensionPixelSize(com.google.android.material.R.dimen.abc_action_bar_default_height_material)
        content.layoutParams = contentParams
        content.setPadding(32, 48, 32, 32)
        layout.addView(content)

        // --- CardView: Основная информация ---
        val infoCard = MaterialCardView(this)
        val infoCardParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        infoCardParams.bottomMargin = 32
        infoCard.layoutParams = infoCardParams
        infoCard.radius = 24f
        infoCard.cardElevation = 8f
        infoCard.setContentPadding(32, 32, 32, 32)

        val infoLayout = LinearLayout(this)
        infoLayout.orientation = LinearLayout.VERTICAL
        infoCard.addView(infoLayout)

        val infoTitle = TextView(this)
        infoTitle.text = "Основная информация"
        infoTitle.textSize = 18f
        infoTitle.setTextColor(0xFF000000.toInt())
        infoTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val infoTitleParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        infoTitleParams.bottomMargin = 12
        infoLayout.addView(infoTitle, infoTitleParams)

        val firstNameLayout = TextInputLayout(this)
        firstNameLayout.hint = "Имя"
        firstNameLayout.boxBackgroundMode = 0
        val firstName = TextInputEditText(this)
        firstNameLayout.addView(firstName)
        infoLayout.addView(firstNameLayout)

        val lastNameLayout = TextInputLayout(this)
        lastNameLayout.hint = "Фамилия"
        lastNameLayout.boxBackgroundMode = 0
        val lastName = TextInputEditText(this)
        lastNameLayout.addView(lastName)
        infoLayout.addView(lastNameLayout)

        val emailLayout = TextInputLayout(this)
        emailLayout.hint = "Email"
        emailLayout.boxBackgroundMode = 0
        val email = TextInputEditText(this)
        email.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailLayout.addView(email)
        infoLayout.addView(emailLayout)

        val saveInfoBtn = Button(this)
        saveInfoBtn.text = "Сохранить"
        saveInfoBtn.setBackgroundResource(R.drawable.green_button)
        saveInfoBtn.setTextColor(0xFFFFFFFF.toInt())
        val saveInfoParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        saveInfoParams.topMargin = 16
        saveInfoBtn.layoutParams = saveInfoParams
        infoLayout.addView(saveInfoBtn)

        content.addView(infoCard)

        // --- CardView: Безопасность ---
        val securityCard = MaterialCardView(this)
        val securityCardParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        securityCard.layoutParams = securityCardParams
        securityCard.radius = 24f
        securityCard.cardElevation = 8f
        securityCard.setContentPadding(32, 32, 32, 32)

        val securityLayout = LinearLayout(this)
        securityLayout.orientation = LinearLayout.VERTICAL
        securityCard.addView(securityLayout)

        val securityTitle = TextView(this)
        securityTitle.text = "Безопасность"
        securityTitle.textSize = 18f
        securityTitle.setTextColor(0xFF000000.toInt())
        securityTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val securityTitleParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        securityTitleParams.bottomMargin = 12
        securityLayout.addView(securityTitle, securityTitleParams)

        val oldPasswordLayout = TextInputLayout(this)
        oldPasswordLayout.hint = "Старый пароль"
        oldPasswordLayout.boxBackgroundMode = 0
        val oldPassword = TextInputEditText(this)
        oldPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        oldPasswordLayout.addView(oldPassword)
        securityLayout.addView(oldPasswordLayout)

        val newPasswordLayout = TextInputLayout(this)
        newPasswordLayout.hint = "Новый пароль"
        newPasswordLayout.boxBackgroundMode = 0
        val newPassword = TextInputEditText(this)
        newPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        newPasswordLayout.addView(newPassword)
        securityLayout.addView(newPasswordLayout)

        val repeatPasswordLayout = TextInputLayout(this)
        repeatPasswordLayout.hint = "Повторите новый пароль"
        repeatPasswordLayout.boxBackgroundMode = 0
        val repeatPassword = TextInputEditText(this)
        repeatPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        repeatPasswordLayout.addView(repeatPassword)
        securityLayout.addView(repeatPasswordLayout)

        val savePasswordBtn = Button(this)
        savePasswordBtn.text = "Сохранить"
        savePasswordBtn.setBackgroundResource(R.drawable.green_button)
        savePasswordBtn.setTextColor(0xFFFFFFFF.toInt())
        val savePassParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        savePassParams.topMargin = 16
        savePasswordBtn.layoutParams = savePassParams
        securityLayout.addView(savePasswordBtn)

        // Кнопка "Выйти со всех устройств"
        val logoutAllBtn = Button(this)
        logoutAllBtn.text = "Выйти со всех устройств"
        logoutAllBtn.setBackgroundResource(R.drawable.red_button)
        logoutAllBtn.setTextColor(0xFFFFFFFF.toInt())
        val logoutAllParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        logoutAllParams.topMargin = 24
        logoutAllBtn.layoutParams = logoutAllParams
        securityLayout.addView(logoutAllBtn)

        logoutAllBtn.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Вы действительно хотите выйти со всех устройств?")
                .setPositiveButton("Выйти") { _, _ ->
                    fullScreenLoading.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        val baseUrl = prefs.getString("server_url", "") ?: ""
                        val api = RetrofitInstance.getApiService(baseUrl, this@ProfileActivity)
                        try {
                            val response = api.logout()
                            if (response.code() == 401) {
                                prefs.edit().remove("auth_token").apply()
                                val intent = android.content.Intent(this@ProfileActivity, MainActivity::class.java)
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                                return@launch
                            }
                            prefs.edit().clear().apply()
                            val intent = android.content.Intent(this@ProfileActivity, MainActivity::class.java)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            showErrorDialog("Ошибка выхода: "+e.message)
                        } finally {
                            fullScreenLoading.visibility = View.GONE
                        }
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        content.addView(securityCard)

        // В самом конце, чтобы overlay был поверх всего
        val overlayParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        fullScreenLoading.layoutParams = overlayParams
        layout.addView(fullScreenLoading)

        setContentView(layout)
        setSupportActionBar(toolbar)

        // --- Загрузка данных пользователя ---
        fullScreenLoading.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val baseUrl = prefs.getString("server_url", "") ?: ""
            val api = RetrofitInstance.getApiService(baseUrl, this@ProfileActivity)
            try {
                val response = api.getMe()
                if (response.isSuccessful) {
                    val user = response.body()
                    firstName.setText(user?.first_name ?: "")
                    lastName.setText(user?.last_name ?: "")
                    email.setText(user?.email ?: "")
                } else if (response.code() == 401) {
                    prefs.edit().remove("auth_token").apply()
                    android.widget.Toast.makeText(this@ProfileActivity, "Авторизационные данные утратили актуальность", android.widget.Toast.LENGTH_LONG).show()
                    val intent = android.content.Intent(this@ProfileActivity, MainActivity::class.java)
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    return@launch
                }
            } finally {
                fullScreenLoading.visibility = View.GONE
            }
        }

        saveInfoBtn.setOnClickListener {
            fullScreenLoading.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = RetrofitInstance.getApiService(baseUrl, this@ProfileActivity)
                try {
                    val response = api.updateMe(
                        ru.dvfu.diplom3d.api.UpdateMeRequest(
                            firstName.text.toString(),
                            lastName.text.toString(),
                            email.text.toString()
                        )
                    )
                    if (response.isSuccessful) {
                        val user = response.body()
                        ru.dvfu.diplom3d.AuthLoadingActivity.userMe = user
                        android.widget.Toast.makeText(this@ProfileActivity, "Данные успешно обновлены", android.widget.Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 401) {
                        prefs.edit().remove("auth_token").apply()
                        android.widget.Toast.makeText(this@ProfileActivity, "Авторизационные данные утратили актуальность", android.widget.Toast.LENGTH_LONG).show()
                        val intent = android.content.Intent(this@ProfileActivity, MainActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                        return@launch
                    } else if (response.code() == 400) {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = StringBuilder()
                        val fieldMap = mapOf(
                            "email" to "Email",
                            "first_name" to "Имя",
                            "last_name" to "Фамилия"
                        )
                        try {
                            val json = JSONObject(errorBody ?: "")
                            for (key in json.keys()) {
                                val arr = json.getJSONArray(key)
                                val field = fieldMap[key] ?: key
                                for (i in 0 until arr.length()) {
                                    errorMsg.append("$field: ${arr.getString(i)}\n")
                                }
                            }
                        } catch (e: Exception) {
                            errorMsg.append(errorBody)
                        }
                        showErrorDialog(errorMsg.toString())
                    } else {
                        showErrorDialog("Ошибка: ${response.code()}\n${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    showErrorDialog("Ошибка сети: ${e.message}")
                } finally {
                    fullScreenLoading.visibility = View.GONE
                }
            }
        }

        savePasswordBtn.setOnClickListener {
            val oldPass = oldPassword.text.toString()
            val newPass = newPassword.text.toString()
            val repeatPass = repeatPassword.text.toString()
            if (oldPass.isEmpty() || newPass.isEmpty() || repeatPass.isEmpty()) {
                showErrorDialog("Заполните все поля для смены пароля")
                return@setOnClickListener
            }
            if (newPass != repeatPass) {
                showErrorDialog("Новые пароли не совпадают")
                return@setOnClickListener
            }
            fullScreenLoading.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = RetrofitInstance.getApiService(baseUrl, this@ProfileActivity)
                try {
                    val response = api.setPassword(
                        ru.dvfu.diplom3d.api.SetPasswordRequest(
                            oldPass, newPass
                        )
                    )
                    if (response.isSuccessful) {
                        android.widget.Toast.makeText(this@ProfileActivity, "Пароль успешно изменён", android.widget.Toast.LENGTH_SHORT).show()
                        oldPassword.setText("")
                        newPassword.setText("")
                        repeatPassword.setText("")
                    } else if (response.code() == 401) {
                        prefs.edit().remove("auth_token").apply()
                        android.widget.Toast.makeText(this@ProfileActivity, "Авторизационные данные утратили актуальность", android.widget.Toast.LENGTH_LONG).show()
                        val intent = android.content.Intent(this@ProfileActivity, MainActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                        return@launch
                    } else if (response.code() == 400) {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = StringBuilder()
                        val fieldMap = mapOf(
                            "current_password" to "Старый пароль",
                            "new_password" to "Новый пароль"
                        )
                        try {
                            val json = org.json.JSONObject(errorBody ?: "")
                            for (key in json.keys()) {
                                val arr = json.getJSONArray(key)
                                val field = fieldMap[key] ?: key
                                for (i in 0 until arr.length()) {
                                    errorMsg.append("$field: ${arr.getString(i)}\n")
                                }
                            }
                        } catch (e: Exception) {
                            errorMsg.append(errorBody)
                        }
                        showErrorDialog(errorMsg.toString())
                    } else {
                        showErrorDialog("Ошибка: ${response.code()}\n${response.errorBody()?.string()}")
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
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
} 