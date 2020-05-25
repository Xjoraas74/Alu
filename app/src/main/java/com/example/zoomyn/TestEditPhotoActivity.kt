package com.example.zoomyn

import android.graphics.BitmapFactory
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

        /*tri.setOnClickListener {
        }

        bi.setOnClickListener {
        }*/

        val bm = BitmapFactory.decodeResource(resources, R.drawable.a_b_11658_6112)
//        imageViewNew.setImageBitmap(bm)
        println("OK!")
    }

    private fun apply() {
        CoroutineScope(Dispatchers.Default).launch {
            // function here will be executed in parallel with actions following this coroutine
        }
        // in parallel with this
    }
}
