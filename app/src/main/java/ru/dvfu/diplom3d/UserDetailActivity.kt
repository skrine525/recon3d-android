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
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Locale

class UserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("user_id", -1)

        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Детализация пользователя"
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
                        val accountIdLabel = TextView(this@UserDetailActivity)
                        accountIdLabel.text = "ID:"
                        accountIdLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                        accountIdLabel.textSize = 16f
                        accountIdLabel.setTextColor(0xFF444444.toInt())
                        val accountIdLabelParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountIdLabelParams.bottomMargin = 2
                        accountLayout.addView(accountIdLabel, accountIdLabelParams)
                        val accountIdView = TextView(this@UserDetailActivity)
                        accountIdView.textSize = 16f
                        accountIdView.setTextColor(0xFF444444.toInt())
                        val accountIdParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountIdParams.bottomMargin = 12
                        accountIdView.text = user.id.toString()
                        accountLayout.addView(accountIdView, accountIdParams)
                        val accountLoginLabel = TextView(this@UserDetailActivity)
                        accountLoginLabel.text = "Логин:"
                        accountLoginLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                        accountLoginLabel.textSize = 16f
                        accountLoginLabel.setTextColor(0xFF444444.toInt())
                        val accountLoginLabelParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountLoginLabelParams.bottomMargin = 2
                        accountLayout.addView(accountLoginLabel, accountLoginLabelParams)
                        val accountLoginView = TextView(this@UserDetailActivity)
                        accountLoginView.textSize = 16f
                        accountLoginView.setTextColor(0xFF444444.toInt())
                        val accountLoginParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountLoginParams.bottomMargin = 12
                        accountLoginView.text = user.username
                        accountLayout.addView(accountLoginView, accountLoginParams)
                        val accountTypeLabel = TextView(this@UserDetailActivity)
                        accountTypeLabel.text = "Тип:"
                        accountTypeLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                        accountTypeLabel.textSize = 16f
                        accountTypeLabel.setTextColor(0xFF444444.toInt())
                        val accountTypeLabelParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountTypeLabelParams.bottomMargin = 2
                        accountLayout.addView(accountTypeLabel, accountTypeLabelParams)
                        val accountTypeView = TextView(this@UserDetailActivity)
                        accountTypeView.textSize = 16f
                        accountTypeView.setTextColor(0xFF444444.toInt())
                        val accountTypeParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountTypeParams.bottomMargin = 12
                        accountTypeView.text = when {
                            user.is_superuser -> "Суперпользователь"
                            user.is_staff -> "Сотрудник"
                            else -> "Пользователь"
                        }
                        accountLayout.addView(accountTypeView, accountTypeParams)
                        val accountDateLabel = TextView(this@UserDetailActivity)
                        accountDateLabel.text = "Регистрация:"
                        accountDateLabel.setTypeface(null, android.graphics.Typeface.BOLD)
                        accountDateLabel.textSize = 16f
                        accountDateLabel.setTextColor(0xFF444444.toInt())
                        val accountDateLabelParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        accountDateLabelParams.bottomMargin = 2
                        accountLayout.addView(accountDateLabel, accountDateLabelParams)
                        val accountDateView = TextView(this@UserDetailActivity)
                        accountDateView.textSize = 16f
                        accountDateView.setTextColor(0xFF444444.toInt())
                        val iso = user.date_joined.replace('T', ' ').replace(Regex("\\..*"), "")
                        val formattedDate = try {
                            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                            formatter.format(parser.parse(iso)!!)
                        } catch (e: Exception) {
                            iso
                        }
                        accountDateView.text = formattedDate
                        accountLayout.addView(accountDateView)
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

                        val firstNameLayout = TextInputLayout(this@UserDetailActivity)
                        firstNameLayout.hint = "Имя"
                        firstNameLayout.boxBackgroundMode = 0
                        val firstName = TextInputEditText(this@UserDetailActivity)
                        firstName.setText(user.first_name)
                        firstNameLayout.addView(firstName)
                        infoLayout.addView(firstNameLayout)

                        val lastNameLayout = TextInputLayout(this@UserDetailActivity)
                        lastNameLayout.hint = "Фамилия"
                        lastNameLayout.boxBackgroundMode = 0
                        val lastName = TextInputEditText(this@UserDetailActivity)
                        lastName.setText(user.last_name)
                        lastNameLayout.addView(lastName)
                        infoLayout.addView(lastNameLayout)

                        val emailLayout = TextInputLayout(this@UserDetailActivity)
                        emailLayout.hint = "Email"
                        emailLayout.boxBackgroundMode = 0
                        val email = TextInputEditText(this@UserDetailActivity)
                        email.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        email.setText(user.email)
                        emailLayout.addView(email)
                        infoLayout.addView(emailLayout)

                        val saveInfoBtn = Button(this@UserDetailActivity)
                        saveInfoBtn.text = "Сохранить"
                        saveInfoBtn.setBackgroundResource(R.drawable.green_button)
                        saveInfoBtn.setTextColor(0xFFFFFFFF.toInt())
                        val saveInfoParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        saveInfoParams.topMargin = 16
                        saveInfoBtn.layoutParams = saveInfoParams
                        infoLayout.addView(saveInfoBtn)
                        
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