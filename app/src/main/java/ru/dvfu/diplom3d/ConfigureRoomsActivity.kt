package ru.dvfu.diplom3d

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import java.util.regex.Pattern
import android.view.GestureDetector
import ru.dvfu.diplom3d.api.RoomsRequest
import ru.dvfu.diplom3d.api.RoomData
import ru.dvfu.diplom3d.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfigureRoomsActivity : AppCompatActivity() {
    data class RoomMarker(val x: Float, val y: Float, val number: String)
    private val markers = mutableListOf<RoomMarker>()
    private lateinit var planImageView: PhotoView
    private lateinit var overlayView: View
    private lateinit var markerContainer: FrameLayout
    private var planUrl: String? = null
    private var planId: String? = null
    private var meshId: String? = null
    private var imageHeight: Int = 0
    private var imageWidth: Int = 0
    private var addingMarker = false
    private lateinit var planFrame: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 48, 32, 32)

        // --- План ---
        planImageView = PhotoView(this)
        planImageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        planImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        // Overlay для затемнения
        overlayView = View(this)
        overlayView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        overlayView.setBackgroundColor(Color.parseColor("#66000000"))
        overlayView.visibility = View.GONE
        // Контейнер для маркеров
        markerContainer = FrameLayout(this)
        markerContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        // Добавляем все в planFrame
        planFrame = FrameLayout(this)
        val planParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        planFrame.layoutParams = planParams
        planFrame.addView(planImageView)
        planFrame.addView(overlayView)
        planFrame.addView(markerContainer)
        layout.addView(planFrame)

        // --- Кнопки ---
        val btnAddRoom = Button(this)
        btnAddRoom.text = "Добавить номер кабинета"
        btnAddRoom.setBackgroundResource(R.drawable.green_button)
        btnAddRoom.setTextColor(Color.WHITE)
        val btnAddRoomParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnAddRoomParams.topMargin = 24
        btnAddRoom.layoutParams = btnAddRoomParams
        layout.addView(btnAddRoom)
        val btnSave = Button(this)
        btnSave.text = "Сохранить"
        btnSave.setBackgroundResource(R.drawable.blue_button)
        btnSave.setTextColor(Color.WHITE)
        val btnSaveParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnSaveParams.topMargin = 24
        btnSave.layoutParams = btnSaveParams
        layout.addView(btnSave)

        setContentView(layout)

        // --- Работа с планом ---
        planId = intent.getStringExtra("plan_id")
        meshId = intent.getStringExtra("mesh_id")
        val planPath = intent.getStringExtra("plan_path")
        if (!planPath.isNullOrEmpty()) {
            val bmp = android.graphics.BitmapFactory.decodeFile(planPath)
            if (bmp != null) {
                planImageView.setImageBitmap(bmp)
                imageWidth = bmp.width
                imageHeight = bmp.height
            } else {
                planImageView.setBackgroundColor(Color.RED)
            }
        } else {
            planImageView.setBackgroundColor(Color.LTGRAY)
        }

        // --- Устанавливаем слушатель на изменение матрицы PhotoView ---
        planImageView.setOnMatrixChangeListener {
            updateMarkers()
        }

        // --- Добавление метки через setOnPhotoTapListener PhotoView ---
        planImageView.setOnPhotoTapListener { _, x, y ->
            if (addingMarker) {
                val drawable = planImageView.drawable ?: return@setOnPhotoTapListener
                val bitmapX = x * drawable.intrinsicWidth
                val bitmapY = y * drawable.intrinsicHeight
                showRoomNumberDialog(bitmapX, bitmapY)
                overlayView.visibility = View.GONE
                addingMarker = false
            }
        }

        btnAddRoom.setOnClickListener {
            addingMarker = true
            overlayView.visibility = View.VISIBLE
            Toast.makeText(this, "Кликните по плану для добавления маркера", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val imgHeight = imageHeight
            val rooms = markers.map { marker ->
                RoomData(
                    number = marker.number,
                    x = marker.x,
                    y = imgHeight - marker.y
                )
            }
            val meshIdInt = meshId?.toIntOrNull()
            if (meshIdInt == null) {
                AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage("Не найден id реконструкции")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val baseUrl = prefs.getString("server_url", "https://dev.radabot.ru") ?: "https://dev.radabot.ru"
            val api = RetrofitInstance.getApiService(baseUrl, this)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = api.postRooms(meshIdInt, RoomsRequest(rooms))
                    if (response.isSuccessful) {
                        AlertDialog.Builder(this@ConfigureRoomsActivity)
                            .setTitle("Успех")
                            .setMessage("Метки успешно сохранены!")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        AlertDialog.Builder(this@ConfigureRoomsActivity)
                            .setTitle("Ошибка")
                            .setMessage("Ошибка сохранения: ${response.code()}\n${response.errorBody()?.string()}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                } catch (e: Exception) {
                    AlertDialog.Builder(this@ConfigureRoomsActivity)
                        .setTitle("Ошибка")
                        .setMessage("Ошибка сохранения: ${e.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }

    private fun showRoomNumberDialog(x: Float, y: Float) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.filters = arrayOf(InputFilter.LengthFilter(5))
        AlertDialog.Builder(this)
            .setTitle("Введите номер кабинета")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val number = input.text.toString().uppercase()
                if (number.matches(Regex("^[A-Za-z]\\d{3}[A-Za-z]?$"))) {
                    addMarker(x, y, number)
                } else {
                    Toast.makeText(this, "Неверный формат номера", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addMarker(x: Float, y: Float, number: String) {
        val marker = TextView(this)
        marker.text = number
        marker.setBackgroundResource(R.drawable.blue_button)
        marker.setTextColor(Color.WHITE)
        marker.gravity = Gravity.CENTER
        val size = 100
        val params = FrameLayout.LayoutParams(size, size)
        marker.layoutParams = params
        markerContainer.addView(marker)
        markers.add(RoomMarker(x, y, number))
        updateMarkers()
        marker.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Удалить?")
                .setMessage("Удалить метку '$number'?")
                .setPositiveButton("Удалить") { _, _ ->
                    markerContainer.removeView(marker)
                    markers.removeAll { it.x == x && it.y == y && it.number == number }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun updateMarkers() {
        val drawable = planImageView.drawable ?: return
        val matrix = planImageView.imageMatrix
        for (i in 0 until markerContainer.childCount) {
            val view = markerContainer.getChildAt(i)
            if (view is TextView && markers.any { it.number == view.text }) {
                val marker = markers.first { it.number == view.text }
                val imgCoords = floatArrayOf(marker.x, marker.y)
                matrix.mapPoints(imgCoords)
                val size = 100
                val params = view.layoutParams as FrameLayout.LayoutParams
                params.leftMargin = (imgCoords[0] - size / 2).toInt()
                params.topMargin = (imgCoords[1] - size / 2).toInt()
                view.layoutParams = params
            }
        }
    }
} 