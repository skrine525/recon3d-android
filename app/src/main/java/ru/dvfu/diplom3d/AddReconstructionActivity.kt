package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Button
import android.view.View
import android.widget.TextView
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dvfu.diplom3d.api.RetrofitInstance
import ru.dvfu.diplom3d.api.UploadPhotoResponse
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import java.net.URL
import android.util.Log
import java.io.FileOutputStream
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class AddReconstructionActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private var croppedUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var photoText: TextView
    private lateinit var photoProgress: ProgressBar
    private var isUploading = false
    private var uploadedPhotoId: String? = null
    private lateinit var maskCard: MaterialCardView
    private lateinit var maskImageView: ImageView
    private lateinit var maskPhotoText: TextView
    private lateinit var btnMask: Button
    private var maskUrl: String? = null
    private lateinit var maskProgress: ProgressBar
    private var savedMaskPath: String? = null
    private lateinit var btnEditMask: Button
    private lateinit var btnCalculateHoughLines: Button
    private lateinit var houghLinesProgress: ProgressBar
    private var uploadedHoughLinesId: String? = null
    private var meshUrl: String? = null
    private var meshId: Int? = null

    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002
        private const val REQUEST_CROP = UCrop.REQUEST_CROP
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Добавление реконструкции"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)

        // --- ScrollView с карточками ---
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

        // --- Цвета для disabled ---
        val grayButtonRes = R.drawable.gray_button
        // --- Карточка 'План помещения' ---
        val card = MaterialCardView(this)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = 32
        card.layoutParams = cardParams
        card.radius = 24f
        card.cardElevation = 8f
        card.setContentPadding(32, 32, 32, 32)
        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL
        val title = TextView(this)
        title.text = "План помещения"
        title.textSize = 20f
        title.setTextColor(0xFF000000.toInt())
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        cardLayout.addView(title)
        val btnPhoto = Button(this)
        btnPhoto.text = "Сфотографировать"
        btnPhoto.setBackgroundResource(R.drawable.green_button)
        btnPhoto.setTextColor(0xFFFFFFFF.toInt())
        val btnPhotoParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnPhotoParams.topMargin = 24
        btnPhoto.layoutParams = btnPhotoParams
        cardLayout.addView(btnPhoto)
        btnPhoto.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnPhoto.setBackgroundResource(if (btnPhoto.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        btnPhoto.setOnFocusChangeListener { _, _ ->
            btnPhoto.setBackgroundResource(if (btnPhoto.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        val btnPick = Button(this)
        btnPick.text = "Выбрать на устройстве"
        btnPick.setBackgroundResource(R.drawable.blue_button)
        btnPick.setTextColor(0xFFFFFFFF.toInt())
        val btnPickParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnPickParams.topMargin = 16
        btnPick.layoutParams = btnPickParams
        cardLayout.addView(btnPick)
        btnPick.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnPick.setBackgroundResource(if (btnPick.isEnabled) R.drawable.blue_button else grayButtonRes)
        }
        btnPick.setOnFocusChangeListener { _, _ ->
            btnPick.setBackgroundResource(if (btnPick.isEnabled) R.drawable.blue_button else grayButtonRes)
        }
        // Серый блок под фото с соотношением 16:9
        val photoBlock = FrameLayout(this)
        photoBlock.setBackgroundColor(0xFFCCCCCC.toInt())
        val photoParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        photoParams.topMargin = 24
        photoBlock.layoutParams = photoParams
        // Устанавливаем высоту 16:9 после layout pass
        photoBlock.post {
            val width = photoBlock.width
            val height = (width * 9f / 16f).toInt()
            val params = photoBlock.layoutParams
            params.height = height
            photoBlock.layoutParams = params
        }
        imageView = ImageView(this)
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        photoBlock.addView(imageView)
        photoText = TextView(this)
        photoText.text = "План"
        photoText.textSize = 18f
        photoText.setTextColor(0xFF888888.toInt())
        photoText.gravity = android.view.Gravity.CENTER
        val photoTextParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        photoText.layoutParams = photoTextParams
        photoBlock.addView(photoText)
        // Круговой прогресс-бар по центру
        photoProgress = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val progressParams = FrameLayout.LayoutParams(128, 128)
        progressParams.gravity = android.view.Gravity.CENTER
        photoProgress.layoutParams = progressParams
        photoProgress.visibility = View.GONE
        photoBlock.addView(photoProgress)
        cardLayout.addView(photoBlock)
        card.addView(cardLayout)
        content.addView(card)

        // --- Новая карточка 'Редактирование маски' ---
        val editMaskCard = MaterialCardView(this)
        val editMaskCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editMaskCardParams.bottomMargin = 32
        editMaskCard.layoutParams = editMaskCardParams
        editMaskCard.radius = 24f
        editMaskCard.cardElevation = 8f
        editMaskCard.setContentPadding(32, 32, 32, 32)
        val editMaskLayout = LinearLayout(this)
        editMaskLayout.orientation = LinearLayout.VERTICAL
        val editMaskTitle = TextView(this)
        editMaskTitle.text = "Маска стен"
        editMaskTitle.textSize = 20f
        editMaskTitle.setTextColor(0xFF000000.toInt())
        editMaskTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        editMaskLayout.addView(editMaskTitle)
        // Кнопка 'Просчитать маску' и прогресс над фото
        btnMask = Button(this)
        btnMask.text = "Просчитать маску"
        btnMask.setBackgroundResource(R.drawable.green_button)
        btnMask.setTextColor(0xFFFFFFFF.toInt())
        btnMask.isEnabled = false // неактивна до загрузки
        val btnMaskParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnMaskParams.topMargin = 16
        btnMask.layoutParams = btnMaskParams
        btnMask.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnMask.setBackgroundResource(if (btnMask.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        btnMask.setOnFocusChangeListener { _, _ ->
            btnMask.setBackgroundResource(if (btnMask.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        // Прогресс-бар для маски внутри кнопки
        val btnMaskContainer = FrameLayout(this)
        btnMaskContainer.layoutParams = btnMaskParams
        btnMaskContainer.addView(btnMask)
        maskProgress = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
        val maskProgressParams = FrameLayout.LayoutParams(64, 64)
        maskProgressParams.gravity = android.view.Gravity.CENTER
        maskProgress.layoutParams = maskProgressParams
        maskProgress.visibility = View.GONE
        btnMaskContainer.addView(maskProgress)
        editMaskLayout.addView(btnMaskContainer)
        // Серый блок под маску с соотношением 16:9
        val displayMetrics = resources.displayMetrics
        val blockWidth = displayMetrics.widthPixels - 64 // padding
        val blockHeight = (blockWidth * 9f / 16f).toInt()
        val maskPhotoBlock = FrameLayout(this)
        maskPhotoBlock.setBackgroundColor(0xFFCCCCCC.toInt())
        val maskPhotoParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            blockHeight
        )
        maskPhotoParams.topMargin = 24
        maskPhotoBlock.layoutParams = maskPhotoParams
        maskImageView = ImageView(this)
        maskImageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        maskImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        maskPhotoBlock.addView(maskImageView)
        maskPhotoText = TextView(this)
        maskPhotoText.text = "Маска"
        maskPhotoText.textSize = 18f
        maskPhotoText.setTextColor(0xFF888888.toInt())
        maskPhotoText.gravity = android.view.Gravity.CENTER
        val maskPhotoTextParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        maskPhotoText.layoutParams = maskPhotoTextParams
        maskPhotoBlock.addView(maskPhotoText)
        maskPhotoText.visibility = View.VISIBLE
        editMaskLayout.addView(maskPhotoBlock)
        // Кнопка 'Редактирование маски'
        btnEditMask = Button(this)
        btnEditMask.text = "Редактирование маски"
        btnEditMask.setBackgroundResource(R.drawable.blue_button)
        btnEditMask.setTextColor(0xFFFFFFFF.toInt())
        val btnEditMaskParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnEditMaskParams.topMargin = 16
        btnEditMask.layoutParams = btnEditMaskParams
        btnEditMask.isEnabled = false
        editMaskLayout.addView(btnEditMask)
        btnEditMask.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnEditMask.setBackgroundResource(if (btnEditMask.isEnabled) R.drawable.blue_button else grayButtonRes)
        }
        btnEditMask.setOnFocusChangeListener { _, _ ->
            btnEditMask.setBackgroundResource(if (btnEditMask.isEnabled) R.drawable.blue_button else grayButtonRes)
        }
        editMaskCard.addView(editMaskLayout)
        content.addView(editMaskCard)

        // --- Новая карточка 'Линии Хафа' ---
        val houghLinesCard = MaterialCardView(this)
        val houghLinesCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        houghLinesCardParams.bottomMargin = 32
        houghLinesCard.layoutParams = houghLinesCardParams
        houghLinesCard.radius = 24f
        houghLinesCard.cardElevation = 8f
        houghLinesCard.setContentPadding(32, 32, 32, 32)
        val houghLinesLayout = LinearLayout(this)
        houghLinesLayout.orientation = LinearLayout.VERTICAL
        val houghLinesTitle = TextView(this)
        houghLinesTitle.text = "Линии Хафа"
        houghLinesTitle.textSize = 20f
        houghLinesTitle.setTextColor(0xFF000000.toInt())
        houghLinesTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        houghLinesLayout.addView(houghLinesTitle)

        btnCalculateHoughLines = Button(this)
        btnCalculateHoughLines.text = "Просчитать линии"
        btnCalculateHoughLines.setBackgroundResource(R.drawable.green_button)
        btnCalculateHoughLines.setTextColor(0xFFFFFFFF.toInt())
        btnCalculateHoughLines.isEnabled = false // Изначально неактивна
        val btnHoughParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnHoughParams.topMargin = 16
        btnCalculateHoughLines.layoutParams = btnHoughParams
        btnCalculateHoughLines.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnCalculateHoughLines.setBackgroundResource(if (btnCalculateHoughLines.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        btnCalculateHoughLines.setOnFocusChangeListener { _, _ ->
            btnCalculateHoughLines.setBackgroundResource(if (btnCalculateHoughLines.isEnabled) R.drawable.green_button else grayButtonRes)
        }

        val btnHoughContainer = FrameLayout(this)
        btnHoughContainer.layoutParams = btnHoughParams
        btnHoughContainer.addView(btnCalculateHoughLines)

        houghLinesProgress = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
        val houghProgressParams = FrameLayout.LayoutParams(64, 64)
        houghProgressParams.gravity = android.view.Gravity.CENTER
        houghLinesProgress.layoutParams = houghProgressParams
        houghLinesProgress.visibility = View.GONE
        btnHoughContainer.addView(houghLinesProgress)
        houghLinesLayout.addView(btnHoughContainer)

        // --- ImageView для результата (аналогично другим карточкам) ---
        val houghPhotoBlock = FrameLayout(this)
        houghPhotoBlock.setBackgroundColor(0xFFCCCCCC.toInt())
        val houghPhotoParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        houghPhotoParams.topMargin = 24
        houghPhotoBlock.layoutParams = houghPhotoParams
        // Устанавливаем высоту 16:9 после layout pass
        houghPhotoBlock.post {
            val width = houghPhotoBlock.width
            val height = (width * 9f / 16f).toInt()
            val params = houghPhotoBlock.layoutParams
            params.height = height
            houghPhotoBlock.layoutParams = params
        }
        val houghImageView = ImageView(this)
        houghImageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        houghImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        houghPhotoBlock.addView(houghImageView)
        val houghPhotoText = TextView(this)
        houghPhotoText.text = "Линии Хафа"
        houghPhotoText.textSize = 18f
        houghPhotoText.setTextColor(0xFF888888.toInt())
        houghPhotoText.gravity = android.view.Gravity.CENTER
        val houghPhotoTextParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        houghPhotoText.layoutParams = houghPhotoTextParams
        houghPhotoBlock.addView(houghPhotoText)
        houghLinesLayout.addView(houghPhotoBlock)

        houghLinesCard.addView(houghLinesLayout)
        content.addView(houghLinesCard)

        // --- Карточка '3D-реконструкция' ---
        val meshCard = MaterialCardView(this)
        val meshCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        meshCardParams.bottomMargin = 32
        meshCard.layoutParams = meshCardParams
        meshCard.radius = 24f
        meshCard.cardElevation = 8f
        meshCard.setContentPadding(32, 32, 32, 32)
        val meshLayout = LinearLayout(this)
        meshLayout.orientation = LinearLayout.VERTICAL
        val meshTitle = TextView(this)
        meshTitle.text = "3D-реконструкция"
        meshTitle.textSize = 20f
        meshTitle.setTextColor(0xFF000000.toInt())
        meshTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        meshLayout.addView(meshTitle)
        // Кнопка 'Построить' с прогресс-баром
        val btnBuildMesh = Button(this)
        btnBuildMesh.text = "Построить"
        btnBuildMesh.setBackgroundResource(R.drawable.green_button)
        btnBuildMesh.setTextColor(0xFFFFFFFF.toInt())
        val btnBuildMeshParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnBuildMeshParams.topMargin = 16
        btnBuildMesh.layoutParams = btnBuildMeshParams
        btnBuildMesh.isEnabled = false // станет активной после расчёта линий Хафа
        btnBuildMesh.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnBuildMesh.setBackgroundResource(if (btnBuildMesh.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        btnBuildMesh.setOnFocusChangeListener { _, _ ->
            btnBuildMesh.setBackgroundResource(if (btnBuildMesh.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        // Кнопка 'Построить' с прогресс-баром
        val btnBuildMeshContainer = FrameLayout(this)
        btnBuildMeshContainer.layoutParams = btnBuildMeshParams
        btnBuildMesh.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        btnBuildMeshContainer.addView(btnBuildMesh)
        val buildMeshProgress = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
        val buildMeshProgressParams = FrameLayout.LayoutParams(64, 64)
        buildMeshProgressParams.gravity = android.view.Gravity.CENTER
        buildMeshProgress.layoutParams = buildMeshProgressParams
        buildMeshProgress.visibility = View.GONE
        btnBuildMeshContainer.addView(buildMeshProgress)
        meshLayout.addView(btnBuildMeshContainer)
        // Кнопка 'Просмотреть'
        val btnViewMesh = Button(this)
        btnViewMesh.text = "Просмотреть"
        btnViewMesh.setBackgroundResource(R.drawable.blue_button)
        btnViewMesh.setTextColor(0xFFFFFFFF.toInt())
        val btnViewMeshParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnViewMeshParams.topMargin = 16
        btnViewMesh.layoutParams = btnViewMeshParams
        btnViewMesh.isEnabled = false // станет активной после успешного построения
        btnViewMesh.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnViewMesh.setBackgroundResource(if (btnViewMesh.isEnabled) R.drawable.blue_button else grayButtonRes)
        }
        btnViewMesh.setOnFocusChangeListener { _, _ ->
            btnViewMesh.setBackgroundResource(if (btnViewMesh.isEnabled) R.drawable.blue_button else grayButtonRes)
        }
        meshLayout.addView(btnViewMesh)
        meshCard.addView(meshLayout)
        content.addView(meshCard)

        // --- Карточка 'Сохранение реконструкции' ---
        val saveCard = MaterialCardView(this)
        val saveCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        saveCardParams.bottomMargin = 32
        saveCard.layoutParams = saveCardParams
        saveCard.radius = 24f
        saveCard.cardElevation = 8f
        saveCard.setContentPadding(32, 32, 32, 32)
        val saveLayout = LinearLayout(this)
        saveLayout.orientation = LinearLayout.VERTICAL
        val saveTitle = TextView(this)
        saveTitle.text = "Сохранение реконструкции"
        saveTitle.textSize = 20f
        saveTitle.setTextColor(0xFF000000.toInt())
        saveTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        saveLayout.addView(saveTitle)
        // Поле для ввода названия (как в AuthActivity)
        val nameInputLayout = TextInputLayout(this)
        nameInputLayout.hint = "Название реконструкции"
        nameInputLayout.boxBackgroundMode = 0
        val nameEdit = TextInputEditText(this)
        nameInputLayout.addView(nameEdit)
        saveLayout.addView(nameInputLayout)
        // Кнопка 'Сохранить'
        val btnSave = Button(this)
        btnSave.text = "Сохранить"
        btnSave.setBackgroundResource(R.drawable.green_button)
        btnSave.setTextColor(0xFFFFFFFF.toInt())
        val btnSaveParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnSaveParams.topMargin = 16
        btnSave.layoutParams = btnSaveParams
        btnSave.isEnabled = false // станет активной после построения модели и если поле не пустое
        saveLayout.addView(btnSave)
        btnSave.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            btnSave.setBackgroundResource(if (btnSave.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        btnSave.setOnFocusChangeListener { _, _ ->
            btnSave.setBackgroundResource(if (btnSave.isEnabled) R.drawable.green_button else grayButtonRes)
        }
        saveCard.addView(saveLayout)
        content.addView(saveCard)
        // Логика активации кнопки
        nameEdit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnSave.isEnabled = !nameEdit.text.isNullOrBlank() && meshId != null
                btnSave.setBackgroundResource(if (btnSave.isEnabled) R.drawable.green_button else grayButtonRes)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        // Также активируем кнопку, если meshId появилось после построения
        // (например, если поле уже заполнено)
        btnBuildMesh.setOnClickListener {
            val planId = uploadedPhotoId
            val maskId = uploadedHoughLinesId // используем id маски, как и для calculate-hough
            if (planId.isNullOrEmpty() || maskId.isNullOrEmpty()) {
                Toast.makeText(this, "Сначала просчитайте линии Хафа", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnBuildMesh.isEnabled = false
            btnBuildMesh.setBackgroundResource(grayButtonRes)
            buildMeshProgress.visibility = View.VISIBLE
            btnBuildMesh.text = ""
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val baseUrl = prefs.getString("server_url", "") ?: ""
                    val api = RetrofitInstance.getApiService(baseUrl, this@AddReconstructionActivity)
                    val response = api.calculateMesh(ru.dvfu.diplom3d.api.CalculateMeshRequest(planId, maskId))
                    if (response.isSuccessful) {
                        val meshResp = response.body()
                        meshUrl = meshResp?.url
                        meshId = meshResp?.id // сохраняем id для кнопки 'Просмотреть'
                        if (meshId != null) {
                            btnViewMesh.isEnabled = true
                            btnViewMesh.setBackgroundResource(R.drawable.blue_button)
                            Toast.makeText(this@AddReconstructionActivity, "3D-модель построена!", Toast.LENGTH_SHORT).show()
                            // --- Активируем кнопку 'Сохранить', если поле заполнено ---
                            btnSave.isEnabled = !nameEdit.text.isNullOrBlank()
                            btnSave.setBackgroundResource(if (btnSave.isEnabled) R.drawable.green_button else grayButtonRes)
                        } else {
                            Toast.makeText(this@AddReconstructionActivity, "Нет id 3D-модели", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@AddReconstructionActivity, "Ошибка построения: ${response.code()}", Toast.LENGTH_LONG).show()
                        btnBuildMesh.isEnabled = true
                        btnBuildMesh.setBackgroundResource(R.drawable.green_button)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AddReconstructionActivity, "Ошибка построения: ${e.message}", Toast.LENGTH_LONG).show()
                    btnBuildMesh.isEnabled = true
                    btnBuildMesh.setBackgroundResource(R.drawable.green_button)
                } finally {
                    buildMeshProgress.visibility = View.GONE
                    btnBuildMesh.text = "Построить"
                }
            }
        }
        btnSave.setOnClickListener {
            val name = nameEdit.text?.toString()?.trim()
            if (name.isNullOrEmpty()) {
                Toast.makeText(this, "Введите название реконструкции", Toast.LENGTH_SHORT).show()
            } else if (meshId == null) {
                Toast.makeText(this, "Сначала постройте 3D-модель", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Сохранить реконструкцию с именем '$name'?")
                    .setPositiveButton("Сохранить") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                                val baseUrl = prefs.getString("server_url", "") ?: ""
                                val api = ru.dvfu.diplom3d.api.RetrofitInstance.getApiService(baseUrl, this@AddReconstructionActivity)
                                val response = api.saveReconstruction(meshId!!, ru.dvfu.diplom3d.api.SaveReconstructionRequest(name))
                                if (response.isSuccessful) {
                                    Toast.makeText(this@AddReconstructionActivity, "Реконструкция успешно сохранена!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@AddReconstructionActivity, "Ошибка сохранения: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@AddReconstructionActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        }
        btnViewMesh.setOnClickListener {
            val meshIdStr = meshId?.toString()
            if (!meshIdStr.isNullOrEmpty()) {
                val intent = Intent(this, ViewMeshActivity::class.java)
                intent.putExtra("mesh_id", meshIdStr)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Нет id 3D-модели", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(layout)
        setSupportActionBar(toolbar)

        // Запрос разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                123
            )
        }

        btnPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }
        btnPick.setOnClickListener {
            dispatchPickFromGalleryIntent()
        }
        // Открытие фото на весь экран по клику
        imageView.setOnClickListener {
            croppedUri?.let {
                FullScreenImageDialogFragment.newInstance(it.toString())
                    .show(supportFragmentManager, "fullscreen_image")
            }
        }
        // Открытие маски на весь экран
        maskImageView.setOnClickListener {
            val maskPath = savedMaskPath ?: (btnEditMask.getTag() as? String)
            if (!maskPath.isNullOrEmpty()) {
                FullScreenImageDialogFragment.newInstance(Uri.fromFile(File(maskPath)).toString())
                    .show(supportFragmentManager, "fullscreen_mask")
            } else if (!maskUrl.isNullOrEmpty()) {
                FullScreenImageDialogFragment.newInstance(maskUrl!!)
                    .show(supportFragmentManager, "fullscreen_mask")
            }
        }
        // Открытие результата Хафа на весь экран
        houghImageView.setOnClickListener {
            val houghPath = houghImageView.getTag(R.id.hough_url_tag) as? String
            if (!houghPath.isNullOrEmpty()) {
                val file = File(houghPath)
                if (file.exists()) {
                    val uri = Uri.fromFile(file).toString()
                    FullScreenImageDialogFragment.newInstance(uri)
                        .show(supportFragmentManager, "fullscreen_hough")
                }
            }
        }
        btnMask.setOnClickListener {
            val fileId = uploadedPhotoId
            if (fileId.isNullOrEmpty()) {
                Toast.makeText(this, "Сначала загрузите фото плана", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnMask.isEnabled = false
            btnMask.setBackgroundResource(grayButtonRes)
            maskProgress.visibility = View.VISIBLE
            btnMask.text = ""
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val baseUrl = prefs.getString("server_url", "") ?: ""
                    val api = RetrofitInstance.getApiService(baseUrl, this@AddReconstructionActivity)
                    val response = api.calculateInitialMask(ru.dvfu.diplom3d.api.CalculateMaskRequest(fileId))
                    if (response.isSuccessful) {
                        val maskResp = response.body()
                        maskUrl = maskResp?.url
                        if (!maskUrl.isNullOrEmpty()) {
                            Glide.with(this@AddReconstructionActivity)
                                .asBitmap()
                                .load(maskUrl)
                                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                        if (resource != null) {
                                            val maskFile = File(cacheDir, "mask_edit_${System.currentTimeMillis()}.jpg")
                                            if (saveBitmapToFile(resource, maskFile)) {
                                                btnEditMask.setTag(maskFile.absolutePath)
                                            }
                                            maskImageView.setImageBitmap(resource)
                                            maskPhotoText.visibility = View.GONE
                                            btnEditMask.isEnabled = true
                                            btnEditMask.setBackgroundResource(R.drawable.blue_button)
                                            btnCalculateHoughLines.isEnabled = true
                                            btnCalculateHoughLines.setBackgroundResource(R.drawable.green_button)
                                        } else {
                                            // ничего не делаем
                                        }
                                    }
                                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                                })
                        }
                    } else {
                        // ничего не делаем
                    }
                } catch (e: Exception) {
                    // ничего не делаем
                } finally {
                    maskProgress.visibility = View.GONE
                    btnMask.text = "Просчитать маску"
                    btnMask.isEnabled = true
                    btnMask.setBackgroundResource(R.drawable.green_button)
                }
            }
        }
        btnEditMask.setOnClickListener {
            val maskPath = savedMaskPath ?: (btnEditMask.getTag() as? String)
            val planPath = photoUri?.path ?: croppedUri?.path // путь к локальному файлу плана
            if (!maskPath.isNullOrEmpty() && !planPath.isNullOrEmpty()) {
                val intent = Intent(this, EditMaskActivity::class.java)
                intent.putExtra("mask_path", maskPath)
                intent.putExtra("plan_path", planPath)
                startActivityForResult(intent, 1234)
            } else {
                Toast.makeText(this, "Сначала просчитайте маску и выберите план", Toast.LENGTH_SHORT).show()
            }
        }
        btnCalculateHoughLines.setOnClickListener {
            val maskPath = savedMaskPath ?: (btnEditMask.getTag() as? String)
            if (maskPath.isNullOrEmpty()) {
                Toast.makeText(this, "Маска не найдена. Сначала просчитайте или отредактируйте маску.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Выполняется загрузка маски...", Toast.LENGTH_SHORT).show()
            btnCalculateHoughLines.isEnabled = false
            btnCalculateHoughLines.setBackgroundResource(grayButtonRes)
            houghLinesProgress.visibility = View.VISIBLE
            btnCalculateHoughLines.text = ""

            val maskFile = File(maskPath)
            if (!maskFile.exists()) {
                Toast.makeText(this, "Файл маски не найден.", Toast.LENGTH_SHORT).show()
                btnCalculateHoughLines.isEnabled = true
                houghLinesProgress.visibility = View.GONE
                btnCalculateHoughLines.text = "Просчитать линии"
                btnCalculateHoughLines.setBackgroundResource(R.drawable.green_button)
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val baseUrl = prefs.getString("server_url", "") ?: ""
                    val api = RetrofitInstance.getApiService(baseUrl, this@AddReconstructionActivity)
                    val requestFile = maskFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", maskFile.name, requestFile)
                    val response = api.uploadUserMask(body)
                    if (response.isSuccessful) {
                       val houghResp = response.body()
                       uploadedHoughLinesId = houghResp?.id
                       Toast.makeText(this@AddReconstructionActivity, "Маска для расчёта линий успешно загружена!", Toast.LENGTH_SHORT).show()
                       val planId = uploadedPhotoId
                       val maskId = houghResp?.id
                       if (!planId.isNullOrEmpty() && !maskId.isNullOrEmpty()) {
                           val houghResponse = api.calculateHough(ru.dvfu.diplom3d.api.CalculateHoughRequest(planId, maskId))
                           if (houghResponse.isSuccessful) {
                               val houghData = houghResponse.body()
                               val houghUrl = houghData?.url
                               if (!houghUrl.isNullOrEmpty()) {
                                   Glide.with(this@AddReconstructionActivity)
                                       .asBitmap()
                                       .load(houghUrl)
                                       .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                                           override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                               if (resource != null) {
                                                   val houghFile = File(cacheDir, "hough_result_${System.currentTimeMillis()}.jpg")
                                                   try {
                                                       FileOutputStream(houghFile).use { out ->
                                                           resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                                       }
                                                       houghImageView.setImageBitmap(resource)
                                                       houghImageView.setTag(R.id.hough_url_tag, houghFile.absolutePath)
                                                       houghPhotoText.visibility = View.GONE
                                                   } catch (e: Exception) {
                                                       // ничего не делаем
                                                   }
                                               } else {
                                                   // ничего не делаем
                                               }
                                               // --- Активируем кнопку 'Построить' 3D ---
                                               btnBuildMesh.isEnabled = true
                                               btnBuildMesh.setBackgroundResource(R.drawable.green_button)
                                           }
                                           override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                                       })
                               } else {
                                   houghPhotoText.visibility = View.VISIBLE
                                   Toast.makeText(this@AddReconstructionActivity, "Пустой url в ответе!", Toast.LENGTH_LONG).show()
                               }
                           } else {
                               val errorBody = houghResponse.errorBody()?.string()
                               Log.e("HoughError", "Code: ${houghResponse.code()}, Body: $errorBody")
                               Toast.makeText(this@AddReconstructionActivity, "Ошибка расчёта линий: ${houghResponse.code()}", Toast.LENGTH_LONG).show()
                           }
                       }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("UploadMaskError", "Code: ${response.code()}, Body: $errorBody")
                        Toast.makeText(this@AddReconstructionActivity, "Ошибка загрузки маски: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("UploadMaskError", "Exception: ${e.message}", e)
                    Toast.makeText(this@AddReconstructionActivity, "Ошибка загрузки маски: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    houghLinesProgress.visibility = View.GONE
                    btnCalculateHoughLines.text = "Просчитать линии"
                    btnCalculateHoughLines.isEnabled = true
                    btnCalculateHoughLines.setBackgroundResource(R.drawable.green_button)
                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                photoUri = FileProvider.getUriForFile(
                    this,
                    "ru.dvfu.diplom3d.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    private fun dispatchPickFromGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
            val path = data.getStringExtra("saved_mask_path")
            if (!path.isNullOrEmpty()) {
                savedMaskPath = path
                val bitmap = BitmapFactory.decodeFile(path)
                maskImageView.setImageBitmap(bitmap)
                maskPhotoText.visibility = View.GONE
                btnEditMask.setTag(path)
                btnEditMask.isEnabled = true
                btnEditMask.setBackgroundResource(R.drawable.blue_button)
                btnCalculateHoughLines.isEnabled = true
                btnCalculateHoughLines.setBackgroundResource(R.drawable.green_button)
            }
        }
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_CAMERA -> {
                photoUri?.let { startCrop(it) }
            }
            REQUEST_GALLERY -> {
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    startCrop(selectedImageUri)
                }
            }
            REQUEST_CROP -> {
                val resultUri = UCrop.getOutput(data!!)
                if (resultUri != null) {
                    croppedUri = resultUri
                    setImageFromUri(resultUri)
                    uploadPhotoToServer(resultUri)
                }
            }
        }
    }

    private fun setImageFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                val exif = ExifInterface(contentResolver.openInputStream(uri)!!)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                imageView.setImageBitmap(rotatedBitmap)
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                photoText.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка отображения фото", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options()
        options.setFreeStyleCropEnabled(true)
        val uCrop = UCrop.of(sourceUri, destUri)
            .withAspectRatio(0f, 0f)
            .withOptions(options)
        uCrop.start(this)
    }

    private fun uploadPhotoToServer(uri: Uri) {
        val file = uriToFile(uri) ?: run {
            Toast.makeText(this, "Не удалось получить файл фото", Toast.LENGTH_SHORT).show()
            return
        }
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val baseUrl = prefs.getString("server_url", "") ?: ""
        val api = RetrofitInstance.getApiService(baseUrl, this)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        isUploading = true
        photoProgress.visibility = View.VISIBLE
        imageView.alpha = 0.3f
        photoText.visibility = View.GONE
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = api.uploadPlanPhoto(body)
                if (response.isSuccessful) {
                    val uploadResp = response.body()
                    uploadedPhotoId = uploadResp?.id
                    imageView.alpha = 1f
                    photoText.visibility = View.GONE
                    // Делаем кнопку активной после загрузки
                    btnMask.isEnabled = true
                    btnMask.setBackgroundResource(R.drawable.green_button)
                } else {
                    Toast.makeText(this@AddReconstructionActivity, "Ошибка загрузки: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddReconstructionActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isUploading = false
                photoProgress.visibility = View.GONE
                imageView.alpha = 1f
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // разрешения даны, можно продолжать
            } else {
                Toast.makeText(this, "Требуются разрешения для работы с фото", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите выйти? Несохранённые изменения будут потеряны.")
            .setPositiveButton("Да") { _, _ -> super.onBackPressed() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private suspend fun downloadAndCacheObj(objUrl: String): File? {
        return try {
            val url = URL(objUrl)
            val connection = url.openConnection()
            connection.connect()
            val input = connection.getInputStream()
            val objFile = File(cacheDir, "mesh_${System.currentTimeMillis()}.obj")
            objFile.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
            objFile
        } catch (e: Exception) {
            null
        }
    }
}

class FullScreenImageDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_URI = "image_uri"
        fun newInstance(imageUri: String): FullScreenImageDialogFragment {
            val fragment = FullScreenImageDialogFragment()
            val args = Bundle()
            args.putString(ARG_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = FrameLayout(requireContext())
        root.setBackgroundColor(0xFF000000.toInt())
        val imageView = ImageView(requireContext())
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        val uriStr = arguments?.getString(ARG_URI)
        if (uriStr != null) {
            imageView.setImageURI(Uri.parse(uriStr))
        }
        root.addView(imageView)
        // Кнопка-крестик
        val closeBtn = ImageButton(requireContext())
        closeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        val size = (48 * resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(size, size)
        params.topMargin = (16 * resources.displayMetrics.density).toInt()
        params.rightMargin = (16 * resources.displayMetrics.density).toInt()
        params.gravity = android.view.Gravity.END or android.view.Gravity.TOP
        closeBtn.layoutParams = params
        closeBtn.setBackgroundColor(0x00000000)
        closeBtn.setOnClickListener { dismiss() }
        root.addView(closeBtn)
        return root
    }
} 