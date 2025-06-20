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

class StaffMainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DrawerLayout
        val drawerLayout = DrawerLayout(this)
        val drawerParams = DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.MATCH_PARENT,
            DrawerLayout.LayoutParams.MATCH_PARENT
        )
        drawerLayout.layoutParams = drawerParams

        // Content (FrameLayout с Toolbar)
        val content = FrameLayout(this)
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
        val user = AuthLoadingActivity.userMe
        val usernameView = TextView(this)
        usernameView.text = user?.username ?: "Пользователь"
        usernameView.textSize = 18f
        usernameView.setPadding(0, 0, 0, 16)
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
                            // Скрываем кнопки, меняем текст на 'Выход...'
                            positive.isEnabled = false
                            negative.isEnabled = false
                            dialog.setMessage("Выход...")
                            // Запуск выхода
                            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                            val baseUrl = prefs.getString("server_url", "") ?: ""
                            CoroutineScope(Dispatchers.Main).launch {
                                val api = RetrofitInstance.getApiService(baseUrl, this@StaffMainMenuActivity)
                                try {
                                    val response = api.logout()
                                    if (response.code() == 204) {
                                        prefs.edit().remove("auth_token").apply()
                                        dialog.dismiss()
                                        startActivity(Intent(this@StaffMainMenuActivity, AuthActivity::class.java))
                                        finish()
                                    } else {
                                        dialog.dismiss()
                                        showErrorDialog("Ошибка выхода: ${response.code()}")
                                    }
                                } catch (e: Exception) {
                                    dialog.dismiss()
                                    showErrorDialog("Ошибка сети: ${e.message}")
                                }
                            }
                        }
                    }
                    dialog.show()
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
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
} 