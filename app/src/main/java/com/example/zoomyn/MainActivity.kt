package com.example.zoomyn

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        //код выбора изображения из галереи
        private const val IMAGE_PICK_CODE = 1000
        //код разрешения
        private const val PERMISSION_CODE = 1001
    }

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

        buttonGame.setOnClickListener {
            Intent(this, TestEditPhotoActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun pickImageFromGallery() {
        //намерение выбрать изображение
        val intent = Intent (Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //разрешение от всплывающего окна предоставлено
                    pickImageFromGallery()
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
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val selectedImage: Uri? = data!!.data
            val i = Intent(this, EditPhotoActivity::class.java)
            i.putExtra("imagePath", selectedImage.toString())
            startActivity(i)
        }
    }
}

