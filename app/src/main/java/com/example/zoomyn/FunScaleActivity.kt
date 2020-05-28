package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_scale.*
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.*
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.buttonCancel
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.buttonDone
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Math.ceil
import java.lang.Math.floor
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class FunScaleActivity : AppCompatActivity() {

    class Mipmap {
        lateinit var self: IntArray
        var width = 0
        var height = 0

        fun create(width: Int, height: Int) {
            this.height = height
            this.width = width
            this.self = IntArray(height * width)
        }
    }

    private val mipmaps = mutableListOf<Mipmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_scale)

        //извлечение изображения из предыдущей активити c примененными фильтрами
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование seekBar'а
        seekBarScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textSeekBarScale.text = "Увеличить в $i " +
                when (i) {
                    2, 3, 4 -> "раза"
                    else -> "раз"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    val value = seekBarScale.progress.toDouble()
                    imageToEdit.setImageBitmap((application as IntermediateResults).scale(currentBitmap, value, mipmaps))
                }
            }
        })

        //функционирование кнопко нижнего меню
        buttonCancel.setOnClickListener {
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(currentBitmap)
            val i = Intent(this, EditPhotoSecondScreenActivity::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            startActivity(i)
        }

        buttonDone.setOnClickListener {
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            val uriCurrentBitmap = bitmapToFile(currentBitmap)
            val i = Intent(this, EditPhotoSecondScreenActivity::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            startActivity(i)
        }

        (application as IntermediateResults).createMipmaps(mipmaps, currentBitmap)
    }

    //функция для получения Uri из Bitmap
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
}
