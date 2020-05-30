package com.example.zoomyn

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        //код выбора изображения из галереи
        private const val IMAGE_PICK_CODE = 1000
        //код разрешения
        private const val PERMISSION_CODE = 1001
        //код взятия изображения из камеры
        private const val IMAGE_CAPTURE_CODE = 1002
    }

    var imageUri: Uri? = null
    var flagCamera: Boolean = false
    var flagGallery: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        //тема для Splash-screen
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonLibrary.setOnClickListener {
            //проверка разрешения среды выполнения
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //разрешение не найдено/отказано
                    flagGallery = true
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    //показать всплывающее окно с запросом разрешения выполнения
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else {
                    //разрешение получено
                    pickImageFromGallery()
                }
            }
            else {
                //ОС is <= Marshmallow
                pickImageFromGallery()
            }
        }

        buttonCamera.setOnClickListener {
            //проверка разрешения среды выполнения
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA ) == PackageManager.PERMISSION_DENIED || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //разрешение не найдено/отказано
                    flagCamera = true
                    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //показать всплывающее окно с запросом разрешения выполнения
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else {
                    //разрешение получено
                    openCamera()
                }
            }
            else {
                //ОС is <= Marshmallow
                openCamera()
            }
        }

//        buttonGame.setOnClickListener {
//            Intent(this, TestEditPhotoActivity::class.java).apply {
//                startActivity(this)
//            }
//        }
    }

    private fun pickImageFromGallery() {
        //намерение выбрать изображение
        val intent = Intent (Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //разрешение от всплывающего окна предоставлено
                   if (flagCamera) {
                       openCamera()
                   }
                    else if (flagGallery) {
                       pickImageFromGallery()
                   }
                }
                else {
                    //разрешение от всплывающего окна отклонено
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //передача изображения из камеры
        if (resultCode == Activity.RESULT_OK) {
            if (imageUri != null) {
                val intent = Intent(this, EditPhotoActivity::class.java)
                intent.putExtra("imagePath", imageUri)
                intent.putExtra("pathToOriginal", imageUri)

                println(imageUri)

                startActivity(intent)
            }
        }
        //передача изображения из галереи
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            try {
                if (uri != null) {
                    val intent = Intent(this, EditPhotoActivity::class.java)
                    intent.putExtra("imagePath", uri)
                    intent.putExtra("pathToOriginal", uri)
                    println(uri)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

