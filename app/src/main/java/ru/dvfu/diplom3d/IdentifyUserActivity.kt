package ru.dvfu.diplom3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentResolver
import androidx.core.content.FileProvider
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.widget.LinearLayout
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import android.app.Activity

class IdentifyUserActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private var imageView: ImageView? = null
    private var photoProgress: ProgressBar? = null
    private var photoText: TextView? = null
    private var uploadedEnvPhotoId: String? = null

    fun uploadPhoto(file: File) {
        photoProgress?.visibility = View.VISIBLE
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val baseUrl = prefs.getString("server_url", "") ?: ""
        val api = ru.dvfu.diplom3d.api.RetrofitInstance.getApiService(baseUrl, this)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = api.uploadUserEnvironmentPhoto(body)
                if (response.isSuccessful) {
                    val resp = response.body()
                    Toast.makeText(this@IdentifyUserActivity, "Фото окружения загружено!", Toast.LENGTH_SHORT).show()
                    uploadedEnvPhotoId = resp?.id
                } else {
                    Toast.makeText(this@IdentifyUserActivity, "Ошибка загрузки: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@IdentifyUserActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                photoProgress?.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        val toolbar = Toolbar(this)
        toolbar.title = "Идентификация пользователя"
        toolbar.layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layout.addView(toolbar)
        val id = intent.getIntExtra("reconstruction_id", -1)

        val scrollView = android.widget.ScrollView(this)
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
        // --- Карточка 'Окружение пользователя' ---
        val card = com.google.android.material.card.MaterialCardView(this)
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
        title.text = "Окружение пользователя"
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
        photoBlock.post {
            val width = photoBlock.width
            val height = (width * 9f / 16f).toInt()
            val params = photoBlock.layoutParams
            params.height = height
            photoBlock.layoutParams = params
        }
        imageView = ImageView(this)
        imageView?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageView?.scaleType = ImageView.ScaleType.CENTER_CROP
        photoBlock.addView(imageView)
        photoText = TextView(this)
        photoText?.text = "Фото окружения"
        photoText?.textSize = 18f
        photoText?.setTextColor(0xFF888888.toInt())
        photoText?.gravity = android.view.Gravity.CENTER
        val photoTextParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        photoText?.layoutParams = photoTextParams
        photoBlock.addView(photoText)
        photoProgress = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val progressParams = FrameLayout.LayoutParams(128, 128)
        progressParams.gravity = android.view.Gravity.CENTER
        photoProgress?.layoutParams = progressParams
        photoProgress?.visibility = View.GONE
        photoBlock.addView(photoProgress)
        cardLayout.addView(photoBlock)
        card.addView(cardLayout)
        content.addView(card)

        // --- Карточка 'Идентификация' ---
        val idCard = com.google.android.material.card.MaterialCardView(this)
        val idCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        idCardParams.bottomMargin = 32
        idCard.layoutParams = idCardParams
        idCard.radius = 24f
        idCard.cardElevation = 8f
        idCard.setContentPadding(32, 32, 32, 32)
        val idLayout = LinearLayout(this)
        idLayout.orientation = LinearLayout.VERTICAL
        val idTitle = TextView(this)
        idTitle.text = "Идентификация"
        idTitle.textSize = 20f
        idTitle.setTextColor(0xFF000000.toInt())
        idTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        idLayout.addView(idTitle)
        val scaleInputLayout = com.google.android.material.textfield.TextInputLayout(this)
        scaleInputLayout.hint = "Размер (число)"
        val scaleEdit = com.google.android.material.textfield.TextInputEditText(this)
        scaleEdit.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        scaleInputLayout.addView(scaleEdit)
        scaleInputLayout.boxBackgroundMode = 0
        idLayout.addView(scaleInputLayout)
        val btnIdentify = Button(this)
        btnIdentify.text = "Произвести идентификацию"
        btnIdentify.setBackgroundResource(R.drawable.green_button)
        btnIdentify.setTextColor(0xFFFFFFFF.toInt())
        val btnIdentifyParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnIdentifyParams.topMargin = 16
        btnIdentify.layoutParams = btnIdentifyParams
        val btnIdentifyContainer = FrameLayout(this)
        btnIdentifyContainer.layoutParams = btnIdentifyParams
        btnIdentifyContainer.addView(btnIdentify)
        val identifyProgress = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
        val identifyProgressParams = FrameLayout.LayoutParams(64, 64)
        identifyProgressParams.gravity = android.view.Gravity.CENTER
        identifyProgress.layoutParams = identifyProgressParams
        identifyProgress.visibility = View.GONE
        btnIdentifyContainer.addView(identifyProgress)
        idLayout.addView(btnIdentifyContainer)
        val scaleInputParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        scaleInputParams.topMargin = 16
        scaleInputLayout.layoutParams = scaleInputParams
        idCard.addView(idLayout)
        content.addView(idCard)

        setContentView(layout)
        setSupportActionBar(toolbar)

        // --- Логика выбора фото ---
        val REQUEST_CAMERA = 2001
        val REQUEST_GALLERY = 2002
        btnPhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = File.createTempFile("env_photo_", ".jpg", cacheDir)
            photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, REQUEST_CAMERA)
        }
        btnPick.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_GALLERY)
        }

        btnIdentify.setOnClickListener {
            val scaleStr = scaleEdit.text?.toString()?.trim()
            val scale = scaleStr?.toIntOrNull()
            if (scale == null) {
                Toast.makeText(this, "Введите корректный размер", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val reconstructionId = id
            val fileId = uploadedEnvPhotoId
            if (fileId.isNullOrEmpty()) {
                Toast.makeText(this, "Сначала загрузите фото окружения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnIdentify.isEnabled = false
            btnIdentify.setBackgroundResource(grayButtonRes)
            identifyProgress.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val baseUrl = prefs.getString("server_url", "") ?: ""
                    val api = ru.dvfu.diplom3d.api.RetrofitInstance.getApiService(baseUrl, this@IdentifyUserActivity)
                    val resp = api.identification(ru.dvfu.diplom3d.api.IdentificationRequest(reconstructionId, fileId, scale))
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body != null) {
                            Toast.makeText(this@IdentifyUserActivity, "Идентификация успешна!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@IdentifyUserActivity, ViewMeshActivity::class.java)
                            intent.putExtra("mesh_id", reconstructionId.toString())
                            intent.putExtra("x", body.x)
                            intent.putExtra("y", body.y)
                            intent.putExtra("z", 50.0)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@IdentifyUserActivity, "Пустой ответ сервера", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@IdentifyUserActivity, "Ошибка идентификации: ${resp.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@IdentifyUserActivity, "Ошибка идентификации: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    btnIdentify.isEnabled = true
                    btnIdentify.setBackgroundResource(R.drawable.green_button)
                    identifyProgress.visibility = View.GONE
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                2001 -> { // REQUEST_CAMERA
                    photoUri?.let { uri ->
                        imageView?.setImageURI(uri)
                        val file = File(uri.path!!)
                        uploadPhoto(file)
                        photoText?.visibility = View.GONE
                    }
                }
                2002 -> { // REQUEST_GALLERY
                    val uri = data?.data
                    if (uri != null) {
                        imageView?.setImageURI(uri)
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
                        cursor?.moveToFirst()
                        val columnIndex = cursor?.getColumnIndex(filePathColumn[0]) ?: -1
                        val picturePath = if (columnIndex >= 0) cursor?.getString(columnIndex) else null
                        cursor?.close()
                        if (!picturePath.isNullOrEmpty()) {
                            val file = File(picturePath)
                            uploadPhoto(file)
                            photoText?.visibility = View.GONE
                        } else {
                            Toast.makeText(this, "Не удалось получить путь к файлу", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
} 