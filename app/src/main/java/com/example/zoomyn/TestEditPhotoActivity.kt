package com.example.zoomyn

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test_edit_photo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TestEditPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_edit_photo)

        val b = createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        for (i in 0 until b.width)
            for (j in 0 until b.height)
                b.setPixel(j, i, Color.BLACK)
        imageViewOrig.setImageBitmap(b)
    }
}
