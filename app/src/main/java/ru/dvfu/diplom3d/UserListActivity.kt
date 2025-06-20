package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.Toast
import android.view.View
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import ru.dvfu.diplom3d.api.RetrofitInstance
import android.widget.EditText
import ru.dvfu.diplom3d.api.UserResponse
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.button.MaterialButton
import android.widget.HorizontalScrollView

class UserListActivity : AppCompatActivity() {
    lateinit var content: LinearLayout
    lateinit var searchEdit: EditText

    // --- Фильтрация по типу пользователя ---
    enum class UserTypeFilter { ALL, USER, STAFF, SUPERUSER }
    var userTypeFilter = UserTypeFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Новый корневой layout
        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Toolbar
        val toolbar = Toolbar(this)
        toolbar.title = "Список пользователей"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        rootLayout.addView(toolbar)

        // --- Material фильтр по типу пользователя ---
        val filterToggleGroup = MaterialButtonToggleGroup(this)
        filterToggleGroup.isSingleSelection = true
        filterToggleGroup.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(32, 32, 32, 0) }
        val btnAll = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
        btnAll.text = "Все"
        btnAll.id = 1
        filterToggleGroup.addView(btnAll)
        val btnUser = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
        btnUser.text = "Пользователь"
        btnUser.id = 2
        filterToggleGroup.addView(btnUser)
        val btnStaff = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
        btnStaff.text = "Сотрудник"
        btnStaff.id = 3
        filterToggleGroup.addView(btnStaff)
        val btnSuper = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
        btnSuper.text = "Суперадминистратор"
        btnSuper.id = 4
        filterToggleGroup.addView(btnSuper)
        filterToggleGroup.check(1) // По умолчанию "Все"
        val filterScroll = HorizontalScrollView(this)
        filterScroll.isHorizontalScrollBarEnabled = false
        filterScroll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        filterScroll.setPadding(32, 0, 32, 0)
        filterScroll.addView(filterToggleGroup)
        rootLayout.addView(filterScroll)
        filterToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                userTypeFilter = when (checkedId) {
                    2 -> UserTypeFilter.USER
                    3 -> UserTypeFilter.STAFF
                    4 -> UserTypeFilter.SUPERUSER
                    else -> UserTypeFilter.ALL
                }
                updateUserList()
            }
        }

        // --- Material поле поиска ---
        val searchInputLayout = TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox)
        searchInputLayout.hint = "Поиск"
        searchInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        searchInputLayout.setPadding(32, 32, 32, 16)
        searchEdit = TextInputEditText(searchInputLayout.context)
        searchEdit.setSingleLine(true)
        searchInputLayout.addView(searchEdit)
        rootLayout.addView(searchInputLayout)

        // --- Отступ между поиском и списком ---
        val space = View(this)
        val spaceParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            32 // px, можно заменить на dp при необходимости
        )
        space.layoutParams = spaceParams
        rootLayout.addView(space)

        // Scrollable list
        val scrollView = ScrollView(this)
        val scrollParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        scrollView.layoutParams = scrollParams
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(32, 48, 32, 32)
        scrollView.addView(content)
        rootLayout.addView(scrollView)

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

        // Итоговый layout
        val frame = FrameLayout(this)
        frame.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        frame.addView(rootLayout)
        frame.addView(fullScreenLoading)
        setContentView(frame)
        setSupportActionBar(toolbar)

        // Загрузка пользователей
        fullScreenLoading.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val baseUrl = prefs.getString("server_url", "") ?: ""
            val api = RetrofitInstance.getApiService(baseUrl, this@UserListActivity)
            try {
                val response = api.getUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    usersList = users
                    updateUserList()
                } else if (response.code() == 401) {
                    prefs.edit().remove("auth_token").apply()
                    Toast.makeText(this@UserListActivity, "Авторизационные данные утратили актуальность", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@UserListActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@UserListActivity, "Ошибка загрузки пользователей: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserListActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                fullScreenLoading.visibility = View.GONE
            }
        }

        // Обновлять список при изменении поиска
        searchEdit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    // --- Список пользователей с поиском ---
    var usersList: List<UserResponse>? = null
    fun updateUserList() {
        content.removeAllViews()
        val users = usersList ?: return
        // Сначала фильтрация по типу
        val filteredByType = when (userTypeFilter) {
            UserTypeFilter.ALL -> users
            UserTypeFilter.USER -> users.filter { !it.is_staff && !it.is_superuser }
            UserTypeFilter.STAFF -> users.filter { it.is_staff && !it.is_superuser }
            UserTypeFilter.SUPERUSER -> users.filter { it.is_superuser }
        }
        // Затем поиск по тексту
        val query = searchEdit.text.toString().trim().lowercase()
        val filtered = if (query.isEmpty()) filteredByType else filteredByType.filter {
            it.first_name.lowercase().contains(query) ||
            it.last_name.lowercase().contains(query) ||
            it.username.lowercase().contains(query) ||
            it.id.toString().contains(query)
        }
        for (user in filtered) {
            val card = MaterialCardView(this@UserListActivity)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.bottomMargin = 24
            card.layoutParams = cardParams
            card.radius = 24f
            card.cardElevation = 8f
            card.setContentPadding(32, 32, 32, 32)

            val cardLayout = LinearLayout(this@UserListActivity)
            cardLayout.orientation = LinearLayout.VERTICAL
            card.addView(cardLayout)

            // Заголовок карточки — display_name
            val titleView = TextView(this@UserListActivity)
            titleView.text = user.display_name
            titleView.textSize = 18f
            titleView.setTypeface(null, android.graphics.Typeface.BOLD)
            titleView.setTextColor(0xFF000000.toInt())
            titleView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            val titleParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            titleParams.bottomMargin = 12
            cardLayout.addView(titleView, titleParams)

            // Вертикальный вывод: каждая пара на новой строке
            fun makeRow(label: String, value: String): LinearLayout {
                val row = LinearLayout(this@UserListActivity)
                row.orientation = LinearLayout.HORIZONTAL
                val labelView = TextView(this@UserListActivity)
                labelView.text = label
                labelView.setTypeface(null, android.graphics.Typeface.BOLD)
                labelView.textSize = 16f
                labelView.setTextColor(0xFF444444.toInt())
                val valueView = TextView(this@UserListActivity)
                valueView.text = value
                valueView.textSize = 16f
                valueView.setTextColor(0xFF444444.toInt())
                row.addView(labelView)
                row.addView(valueView)
                return row
            }
            cardLayout.addView(makeRow("ID: ", user.id.toString()))
            cardLayout.addView(makeRow("Логин: ", user.username))
            cardLayout.addView(makeRow("Тип: ", when {
                user.is_superuser -> "Суперпользователь"
                user.is_staff -> "Сотрудник"
                else -> "Пользователь"
            }))

            card.setOnClickListener {
                val intent = Intent(this@UserListActivity, UserDetailActivity::class.java)
                intent.putExtra("user_id", user.id)
                startActivity(intent)
            }
            content.addView(card)
        }
    }
} 