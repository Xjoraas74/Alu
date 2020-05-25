package com.example.zoomyn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_test_edit_photo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*

class TestEditPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_edit_photo)

        /*val value = .025

        tri.setOnClickListener {
            imageViewNew.setImageBitmap(scale((imageViewOrig.drawable as BitmapDrawable).bitmap, value, mipmaps))
        }

        bi.setOnClickListener {
            val obit = createBitmap((imageViewOrig.drawable as BitmapDrawable).bitmap)
            val o = IntArray(obit.width * obit.height)
            obit.getPixels(o, 0, obit.width, 0, 0, obit.width, obit.height)
            val b = createBitmap((obit.width*value).roundToInt(),(obit.height*value).roundToInt(),Bitmap.Config.ARGB_8888)
            b.setPixels(getPixelsBilinearlyScaled(o, value, obit.width, obit.height), 0, b.width, 0, 0, b.width, b.height)
            imageViewNew.setImageBitmap(b)
        }*/
    }

    


    // allegedly uses trilinear filtering for downscaling, don't delete it, don't use it


    private fun apply() {
        CoroutineScope(Dispatchers.Default).launch {
            // function here will be executed in parallel with actions following this coroutine
        }
        // in parallel with this
    }
}
