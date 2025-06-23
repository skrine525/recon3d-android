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

class AddReconstructionActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private var croppedUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var photoText: TextView

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
        photoText.text = "Фото"
        photoText.textSize = 18f
        photoText.setTextColor(0xFF888888.toInt())
        photoText.gravity = android.view.Gravity.CENTER
        val photoTextParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        photoText.layoutParams = photoTextParams
        photoBlock.addView(photoText)
        cardLayout.addView(photoBlock)
        card.addView(cardLayout)
        content.addView(card)

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