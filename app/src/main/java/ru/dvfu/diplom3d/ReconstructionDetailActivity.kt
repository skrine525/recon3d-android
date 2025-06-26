package ru.dvfu.diplom3d

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.LinearLayout
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance
import ru.dvfu.diplom3d.api.CalculateMeshResponse
import ru.dvfu.diplom3d.api.UserResponse
import android.widget.FrameLayout

class ReconstructionDetailActivity : AppCompatActivity() {
    private lateinit var editFullScreenLoading: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val toolbar = Toolbar(this)
        toolbar.title = "Детали реконструкции"
        layout.addView(toolbar)
        setSupportActionBar(toolbar)
        val id = intent.getIntExtra("reconstruction_id", -1)

        // Карточка "Информация"
        val infoCard = com.google.android.material.card.MaterialCardView(this)
        val infoCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        infoCardParams.topMargin = 32
        infoCardParams.bottomMargin = 32
        infoCard.layoutParams = infoCardParams
        infoCard.radius = 24f
        infoCard.cardElevation = 8f
        infoCard.setContentPadding(32, 32, 32, 32)
        val infoLayout = LinearLayout(this)
        infoLayout.orientation = LinearLayout.VERTICAL
        infoLayout.setPadding(0, 0, 0, 0)
        infoCard.addView(infoLayout)

        val infoTitle = TextView(this)
        infoTitle.text = "Информация"
        infoTitle.textSize = 20f
        infoTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        infoTitle.setTextColor(0xFF000000.toInt())
        val infoTitleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        infoTitleParams.bottomMargin = 24
        infoLayout.addView(infoTitle, infoTitleParams)

        // Прогрессбар для карточки и содержимое в FrameLayout
        val infoFrame = FrameLayout(this)
        val infoFrameParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        infoFrame.layoutParams = infoFrameParams
        infoLayout.addView(infoFrame)

        val infoProgress = android.widget.ProgressBar(this)
        val infoProgressParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        infoProgressParams.gravity = android.view.Gravity.CENTER
        infoProgress.layoutParams = infoProgressParams
        infoFrame.addView(infoProgress)

        val infoContentLayout = LinearLayout(this)
        infoContentLayout.orientation = LinearLayout.VERTICAL
        val infoContentParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        infoContentLayout.layoutParams = infoContentParams
        infoFrame.addView(infoContentLayout)
        infoContentLayout.visibility = View.INVISIBLE

        fun makeLabel(text: String): TextView {
            val tv = TextView(this)
            tv.text = text
            tv.setTypeface(null, android.graphics.Typeface.BOLD)
            tv.textSize = 16f
            tv.setTextColor(0xFF444444.toInt())
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.bottomMargin = 2
            tv.layoutParams = params
            return tv
        }
        fun makeValue(): TextView {
            val tv = TextView(this)
            tv.textSize = 16f
            tv.setTextColor(0xFF444444.toInt())
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.bottomMargin = 12
            tv.layoutParams = params
            return tv
        }

        val idLabel = makeLabel("ID:")
        val idValue = makeValue()
        infoContentLayout.addView(idLabel)
        infoContentLayout.addView(idValue)

        val nameLabel = makeLabel("Название:")
        val nameValue = makeValue()
        infoContentLayout.addView(nameLabel)
        infoContentLayout.addView(nameValue)

        val createdByLabel = makeLabel("Создатель:")
        val createdByValue = makeValue()
        infoContentLayout.addView(createdByLabel)
        infoContentLayout.addView(createdByValue)

        val createdAtLabel = makeLabel("Время сохранения:")
        val createdAtValue = makeValue()
        infoContentLayout.addView(createdAtLabel)
        infoContentLayout.addView(createdAtValue)

        layout.addView(infoCard)

        // Карточка с кнопками
        val card = com.google.android.material.card.MaterialCardView(this)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.topMargin = 32
        card.layoutParams = cardParams
        card.radius = 20f
        card.cardElevation = 6f
        card.setContentPadding(32, 32, 32, 32)
        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL

        val cardTitle = TextView(this)
        cardTitle.text = "Визуализация"
        cardTitle.textSize = 20f
        cardTitle.setTextColor(0xFF000000.toInt())
        cardTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        cardLayout.addView(cardTitle)
        val space = View(this)
        space.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 24)
        cardLayout.addView(space)

        val btnView = android.widget.Button(this)
        btnView.text = "Просмотреть"
        btnView.setBackgroundResource(R.drawable.blue_button)
        btnView.setTextColor(0xFFFFFFFF.toInt())
        btnView.setOnClickListener {
            val intent = android.content.Intent(this, ViewMeshActivity::class.java)
            intent.putExtra("mesh_id", id.toString())
            startActivity(intent)
        }
        cardLayout.addView(btnView)

        val btnIdentify = android.widget.Button(this)
        btnIdentify.text = "Идентифицировать пользователя"
        btnIdentify.setBackgroundResource(R.drawable.green_button)
        btnIdentify.setTextColor(0xFFFFFFFF.toInt())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 16
        btnIdentify.layoutParams = params
        btnIdentify.setOnClickListener {
            android.widget.Toast.makeText(this, "Идентификация пользователя (заглушка)", android.widget.Toast.LENGTH_SHORT).show()
        }
        cardLayout.addView(btnIdentify)

        card.addView(cardLayout)
        layout.addView(card)

        // Карточка 'Редактирование' только для staff/superuser
        val currentUser = ru.dvfu.diplom3d.AuthLoadingActivity.userMe
        if (currentUser?.is_staff == true || currentUser?.is_superuser == true) {
            val editCard = com.google.android.material.card.MaterialCardView(this)
            val editCardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            editCardParams.topMargin = 32
            editCard.layoutParams = editCardParams
            editCard.radius = 24f
            editCard.cardElevation = 8f
            editCard.setContentPadding(32, 32, 32, 32)
            val editLayout = LinearLayout(this)
            editLayout.orientation = LinearLayout.VERTICAL
            val editTitle = TextView(this)
            editTitle.text = "Редактирование"
            editTitle.textSize = 20f
            editTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            editTitle.setTextColor(0xFF000000.toInt())
            val editTitleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            editTitleParams.bottomMargin = 24
            editLayout.addView(editTitle, editTitleParams)

            // Поле для редактирования названия
            val nameInputLayout = com.google.android.material.textfield.TextInputLayout(this)
            nameInputLayout.hint = "Название"
            nameInputLayout.boxBackgroundMode = 0
            val nameEdit = com.google.android.material.textfield.TextInputEditText(this)
            nameInputLayout.addView(nameEdit)
            editLayout.addView(nameInputLayout)

            // Кнопка Сохранить
            val btnSave = android.widget.Button(this)
            btnSave.text = "Сохранить"
            btnSave.setBackgroundResource(R.drawable.green_button)
            btnSave.setTextColor(0xFFFFFFFF.toInt())
            val btnSaveParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            btnSaveParams.topMargin = 16
            btnSave.layoutParams = btnSaveParams
            editLayout.addView(btnSave)

            // Кнопка Удалить
            val btnDelete = android.widget.Button(this)
            btnDelete.text = "Удалить"
            btnDelete.setBackgroundResource(R.drawable.red_button)
            btnDelete.setTextColor(0xFFFFFFFF.toInt())
            val btnDeleteParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            btnDeleteParams.topMargin = 16
            btnDelete.layoutParams = btnDeleteParams
            editLayout.addView(btnDelete)

            // Логика: подставить текущее название
            nameEdit.setText(nameValue.text)

            // --- Обработчики кнопок ---
            btnSave.setOnClickListener {
                val newName = nameEdit.text?.toString()?.trim()
                if (newName.isNullOrEmpty()) {
                    android.widget.Toast.makeText(this, "Введите название", android.widget.Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                btnSave.isEnabled = false
                editFullScreenLoading.visibility = View.VISIBLE
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val baseUrl = prefs.getString("server_url", "") ?: ""
                val api = ru.dvfu.diplom3d.api.RetrofitInstance.getApiService(baseUrl, this)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        val resp = api.patchReconstruction(id, ru.dvfu.diplom3d.api.PatchReconstructionRequest(newName))
                        if (resp.isSuccessful) {
                            android.widget.Toast.makeText(this@ReconstructionDetailActivity, "Название обновлено", android.widget.Toast.LENGTH_SHORT).show()
                            // Обновить инфу в карточке
                            nameValue.text = newName
                        } else {
                            android.widget.Toast.makeText(this@ReconstructionDetailActivity, "Ошибка: ${resp.code()}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(this@ReconstructionDetailActivity, "Ошибка: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    } finally {
                        btnSave.isEnabled = true
                        editFullScreenLoading.visibility = View.GONE
                    }
                }
            }

            btnDelete.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Удалить реконструкцию?")
                    .setPositiveButton("Удалить") { _, _ ->
                        btnDelete.isEnabled = false
                        editFullScreenLoading.visibility = View.VISIBLE
                        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        val baseUrl = prefs.getString("server_url", "") ?: ""
                        val api = ru.dvfu.diplom3d.api.RetrofitInstance.getApiService(baseUrl, this)
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                val resp = api.deleteReconstruction(id)
                                if (resp.isSuccessful) {
                                    android.widget.Toast.makeText(this@ReconstructionDetailActivity, "Удалено", android.widget.Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    android.widget.Toast.makeText(this@ReconstructionDetailActivity, "Ошибка: ${resp.code()}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(this@ReconstructionDetailActivity, "Ошибка: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            } finally {
                                btnDelete.isEnabled = true
                                editFullScreenLoading.visibility = View.GONE
                            }
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }

            editCard.addView(editLayout)
            layout.addView(editCard)
        }

        // --- Полноэкранный ProgressBar для операций редактирования (как в AuthActivity) ---
        editFullScreenLoading = FrameLayout(this)
        editFullScreenLoading.setBackgroundColor(0x80000000.toInt())
        editFullScreenLoading.visibility = View.GONE
        editFullScreenLoading.isClickable = true
        editFullScreenLoading.isFocusable = true
        val editProgressBar = android.widget.ProgressBar(this)
        val editPbParams = FrameLayout.LayoutParams(128, 128)
        editPbParams.gravity = android.view.Gravity.CENTER
        editProgressBar.layoutParams = editPbParams
        editFullScreenLoading.addView(editProgressBar)
        val editOverlayParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        editFullScreenLoading.layoutParams = editOverlayParams

        setContentView(layout)
        (this.findViewById<android.view.ViewGroup>(android.R.id.content)).addView(editFullScreenLoading)

        // Скрываем содержимое карточки до загрузки
        infoContentLayout.visibility = View.INVISIBLE
        infoProgress.visibility = View.VISIBLE

        // Загрузка информации о реконструкции и пользователе
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val baseUrl = prefs.getString("server_url", "") ?: ""
        val api = RetrofitInstance.getApiService(baseUrl, this)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val resp = api.getReconstructionById(id)
                if (resp.isSuccessful) {
                    val recon = resp.body()
                    idValue.text = recon?.id?.toString() ?: "-"
                    nameValue.text = recon?.name ?: "-"
                    createdAtValue.text = recon?.created_at?.let { raw ->
                        try {
                            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                            val outputFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                            val date = inputFormat.parse(raw.substring(0, 19))
                            if (date != null) outputFormat.format(date) else raw
                        } catch (e: Exception) {
                            raw
                        }
                    } ?: "-"
                    if (recon?.created_by != null) {
                        val userResp = api.getUser(recon.created_by)
                        if (userResp.isSuccessful) {
                            val user = userResp.body()
                            createdByValue.text = user?.display_name ?: user?.username ?: recon.created_by.toString()
                        } else {
                            createdByValue.text = recon.created_by.toString()
                        }
                    } else {
                        createdByValue.text = "-"
                    }
                } else {
                    idValue.text = "Ошибка загрузки информации"
                }
            } catch (e: Exception) {
                idValue.text = "Ошибка: ${e.localizedMessage}"
            } finally {
                // Показываем содержимое карточки, убираем прогрессбар
                infoContentLayout.visibility = View.VISIBLE
                infoProgress.visibility = View.INVISIBLE
            }
        }
    }
} 