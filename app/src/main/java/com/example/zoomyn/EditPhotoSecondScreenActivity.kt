package com.example.zoomyn

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*


class EditPhotoSecondScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo_second_screen)

        //извлечение изображения из предыдущей активити
        val bitmap = intent.getParcelableExtra("BitmapImage") as Bitmap

        imageToEditSecondScreen.setImageBitmap(bitmap)
    }
}
