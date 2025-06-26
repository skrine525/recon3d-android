package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.view.Gravity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.widget.LinearLayout
import android.view.View
import android.content.Intent
import android.app.AlertDialog
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.text.TextWatcher

class StaffMainMenuActivity : AppCompatActivity() {
    private var usernameView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DrawerLayout
        val drawerLayout = DrawerLayout(this)
        val drawerParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.MATCH_PARENT,
            DrawerLayout.LayoutParams.MATCH_PARENT
        )
        drawerLayout.layoutParams = drawerParams

        // Content (LinearLayout с Toolbar и карточками)
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.layoutParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.MATCH_PARENT,
            DrawerLayout.LayoutParams.MATCH_PARENT
        )

        // Toolbar
        val toolbar = Toolbar(this)
        toolbar.title = "Режим сотрудника"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        content.addView(toolbar)

        // --- Карточки под тулбаром ---
        val cardsLayout = LinearLayout(this)
        cardsLayout.orientation = LinearLayout.VERTICAL
        cardsLayout.setPadding(32, 48, 32, 32)
        // --- Карточка 3D-реконструкция ---
        val card3d = MaterialCardView(this)
        val card3dParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        card3dParams.bottomMargin = 32
        card3d.layoutParams = card3dParams
        card3d.radius = 24f
        card3d.cardElevation = 8f
        card3d.setContentPadding(32, 32, 32, 32)
        val layout3d = LinearLayout(this)
        layout3d.orientation = LinearLayout.VERTICAL
        val title3d = TextView(this)
        title3d.text = "Управление реконструкциями"
        title3d.textSize = 20f
        title3d.setTextColor(0xFF000000.toInt())
        title3d.setTypeface(null, android.graphics.Typeface.BOLD)
        layout3d.addView(title3d)
        val btnAdd = android.widget.Button(this)
        btnAdd.text = "Добавление реконструкции"
        btnAdd.setBackgroundResource(R.drawable.green_button)
        btnAdd.setTextColor(0xFFFFFFFF.toInt())
        val btnAddParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnAddParams.topMargin = 24
        btnAdd.layoutParams = btnAddParams
        layout3d.addView(btnAdd)
        card3d.addView(layout3d)
        cardsLayout.addView(card3d)
        // --- Карточка Идентификация пользователя ---
        val cardIdent = MaterialCardView(this)
        val cardIdentParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardIdentParams.bottomMargin = 32
        cardIdent.layoutParams = cardIdentParams
        cardIdent.radius = 24f
        cardIdent.cardElevation = 8f
        cardIdent.setContentPadding(32, 32, 32, 32)
        val layoutIdent = LinearLayout(this)
        layoutIdent.orientation = LinearLayout.VERTICAL
        val titleIdent = TextView(this)
        titleIdent.text = "Список реконструкций"
        titleIdent.textSize = 20f
        titleIdent.setTextColor(0xFF000000.toInt())
        titleIdent.setTypeface(null, android.graphics.Typeface.BOLD)
        layoutIdent.addView(titleIdent)
        // Отступ после тайтла
        val spaceAfterTitle = View(this)
        val spaceParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            24 // px, аналогично btnAddParams.topMargin
        )
        spaceAfterTitle.layoutParams = spaceParams
        layoutIdent.addView(spaceAfterTitle)
        // --- Поисковый инпут ---
        val searchInputLayout = TextInputLayout(this)
        val searchEditText = TextInputEditText(this)
        searchInputLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        searchInputLayout.hint = "Поиск по названию"
        searchInputLayout.boxBackgroundMode = 0 // BOX_NONE
        searchInputLayout.addView(searchEditText)
        layoutIdent.addView(searchInputLayout)
        // --- Прогрессбар и контейнер для кнопок ---
        val reconProgress = android.widget.ProgressBar(this)
        layoutIdent.addView(reconProgress)
        val reconButtonsLayout = LinearLayout(this)
        reconButtonsLayout.orientation = LinearLayout.VERTICAL
        layoutIdent.addView(reconButtonsLayout)
        cardIdent.addView(layoutIdent)
        cardsLayout.addView(cardIdent)
        content.addView(cardsLayout)
        drawerLayout.addView(content)

        // NavigationView (sidebar)
        val navView = NavigationView(this)
        val navParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.WRAP_CONTENT,
            DrawerLayout.LayoutParams.MATCH_PARENT
        )
        navParams.gravity = Gravity.START
        navView.layoutParams = navParams

        // Username и разделитель как header
        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.VERTICAL
        headerLayout.setPadding(32, 64, 32, 16)
        usernameView = TextView(this)
        usernameView?.text = AuthLoadingActivity.userMe?.display_name ?: "Пользователь"
        usernameView?.textSize = 18f
        usernameView?.setPadding(0, 0, 0, 16)
        headerLayout.addView(usernameView)
        // Divider
        val divider = View(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.layoutParams = params
        divider.setBackgroundColor(0xFFCCCCCC.toInt())
        headerLayout.addView(divider)
        // Прогрессбар (скрыт по умолчанию)
        val progressBar = android.widget.ProgressBar(this)
        progressBar.visibility = View.GONE
        headerLayout.addView(progressBar)
        navView.addHeaderView(headerLayout)
        // Основные пункты
        val menu: Menu = navView.menu
        menu.add("Профиль")
        menu.add("Список пользователей")
        val logoutItem = menu.add("Выйти")
        navView.setNavigationItemSelectedListener { item ->
            when (item.title) {
                "Выйти" -> {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Подтверждение")
                        .setMessage("Вы действительно хотите выйти?")
                        .setPositiveButton("Выйти", null)
                        .setNegativeButton("Отмена", null)
                        .create()
                    dialog.setOnShowListener {
                        val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        val negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        positive.setOnClickListener {
                            positive.isEnabled = false
                            negative.isEnabled = false
                            dialog.setMessage("Выход...")
                            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                            prefs.edit().clear().apply()
                            dialog.dismiss()
                            val intent = Intent(this@StaffMainMenuActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                    dialog.show()
                    true
                }
                "Список пользователей" -> {
                    startActivity(Intent(this, UserListActivity::class.java))
                    true
                }
                "Профиль" -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        drawerLayout.addView(navView)
        setContentView(drawerLayout)

        // Настраиваем Toolbar как ActionBar
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            0, 0 // не используем строковые ресурсы, чтобы не было ошибки
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Добавляем обработчик нажатия на кнопку 'Добавление реконструкции'
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddReconstructionActivity::class.java)
            startActivity(intent)
        }

        // Загрузка реконструкций
        var allReconstructions: List<ru.dvfu.diplom3d.api.ReconstructionListItem> = emptyList()
        fun showFilteredReconstructions(query: String) {
            reconButtonsLayout.removeAllViews()
            val filtered = if (query.isBlank()) allReconstructions else allReconstructions.filter { it.name.contains(query, ignoreCase = true) }
            for (item in filtered) {
                val btn = android.widget.Button(this@StaffMainMenuActivity)
                btn.text = item.name
                btn.setBackgroundResource(R.drawable.blue_button)
                btn.setTextColor(0xFFFFFFFF.toInt())
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 16
                btn.layoutParams = params
                btn.setOnClickListener {
                    val intent = Intent(this@StaffMainMenuActivity, ReconstructionDetailActivity::class.java)
                    intent.putExtra("reconstruction_id", item.id)
                    startActivity(intent)
                }
                reconButtonsLayout.addView(btn)
            }
            if (filtered.isEmpty()) {
                val tv = TextView(this@StaffMainMenuActivity)
                tv.text = "Ничего не найдено"
                tv.setTextColor(0xFF888888.toInt())
                reconButtonsLayout.addView(tv)
            }
        }
        fun loadReconstructions() {
            reconProgress.visibility = View.VISIBLE
            reconButtonsLayout.removeAllViews()
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val baseUrl = prefs.getString("server_url", "") ?: ""
            val api = RetrofitInstance.getApiService(baseUrl, this)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = api.getReconstructions()
                    if (response.isSuccessful) {
                        allReconstructions = response.body() ?: emptyList()
                        showFilteredReconstructions(searchEditText.text?.toString() ?: "")
                    } else {
                        val tv = TextView(this@StaffMainMenuActivity)
                        tv.text = "Ошибка загрузки: ${response.code()}"
                        tv.setTextColor(0xFFFF0000.toInt())
                        reconButtonsLayout.addView(tv)
                    }
                } catch (e: Exception) {
                    val tv = TextView(this@StaffMainMenuActivity)
                    tv.text = "Ошибка: ${e.localizedMessage}"
                    tv.setTextColor(0xFFFF0000.toInt())
                    reconButtonsLayout.addView(tv)
                } finally {
                    reconProgress.visibility = View.GONE
                }
            }
        }
        // Фильтрация по изменению текста
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showFilteredReconstructions(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        loadReconstructions()
    }

    override fun onResume() {
        super.onResume()
        // Обновить имя в сайдбаре
        val user = AuthLoadingActivity.userMe
        usernameView?.text = user?.display_name ?: "Пользователь"
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
} 