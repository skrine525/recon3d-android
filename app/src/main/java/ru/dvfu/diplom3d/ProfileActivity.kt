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
import androidx.cardview.widget.CardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance
import org.json.JSONObject

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
        val infoCard = CardView(this)
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
        infoTitle.setPadding(0, 0, 0, 16)
        infoLayout.addView(infoTitle)

        val firstName = EditText(this)
        firstName.hint = "Имя"
        infoLayout.addView(firstName)

        val lastName = EditText(this)
        lastName.hint = "Фамилия"
        infoLayout.addView(lastName)

        val email = EditText(this)
        email.hint = "Email"
        email.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        infoLayout.addView(email)

        val saveInfoBtn = Button(this)
        saveInfoBtn.text = "Сохранить"
        val saveInfoParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        saveInfoParams.topMargin = 16
        saveInfoBtn.layoutParams = saveInfoParams
        infoLayout.addView(saveInfoBtn)

        content.addView(infoCard)

        // --- CardView: Безопасность ---
        val securityCard = CardView(this)
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
        securityTitle.setPadding(0, 0, 0, 16)
        securityLayout.addView(securityTitle)

        val oldPassword = EditText(this)
        oldPassword.hint = "Старый пароль"
        oldPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        securityLayout.addView(oldPassword)

        val newPassword = EditText(this)
        newPassword.hint = "Новый пароль"
        newPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        securityLayout.addView(newPassword)

        val repeatPassword = EditText(this)
        repeatPassword.hint = "Повторите новый пароль"
        repeatPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        securityLayout.addView(repeatPassword)

        val savePasswordBtn = Button(this)
        savePasswordBtn.text = "Сохранить"
        val savePassParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        savePassParams.topMargin = 16
        savePasswordBtn.layoutParams = savePassParams
        securityLayout.addView(savePasswordBtn)

        content.addView(securityCard)

        // --- Полноэкранный ProgressBar ---
        val fullScreenLoading = FrameLayout(this)
        fullScreenLoading.setBackgroundColor(0x80000000.toInt())
        fullScreenLoading.visibility = View.VISIBLE
        fullScreenLoading.isClickable = true
        fullScreenLoading.isFocusable = true
        val progressBar = android.widget.ProgressBar(this)
        val pbParams = FrameLayout.LayoutParams(128, 128)
        pbParams.gravity = android.view.Gravity.CENTER
        progressBar.layoutParams = pbParams
        fullScreenLoading.addView(progressBar)
        layout.addView(fullScreenLoading)

        setContentView(layout)
        setSupportActionBar(toolbar)

        // --- Загрузка данных пользователя ---
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
    }

    private fun showErrorDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
} 