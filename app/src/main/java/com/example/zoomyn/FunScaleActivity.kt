package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_scale.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
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

    var scale = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_scale)

        //получение фотографии
        val fileUri: Uri = intent.getParcelableExtra("imagePath")
        val pathToOriginal: Uri = intent.getParcelableExtra("pathToOriginal")

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
        val cancelBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование seekBar'а
        seekBarScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textSeekBarScale.text = "Увеличить в ${i.toDouble() / 10} " +
                        when (i) {
                            10 -> "раз"
                            else -> "раза"
                        }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    scale = seekBarScale.progress.toDouble() / 10
                    imageToEdit.setImageBitmap((application as IntermediateResults).scale(currentBitmap, scale, mipmaps))
                }
            }
        })

        //функционирование кнопко нижнего меню
        buttonCancel.setOnClickListener {
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(cancelBitmap)
            val intentCancel = Intent(this, EditPhotoSecondScreenActivity::class.java)
            intentCancel.putExtra("imagePath", uriCurrentBitmap)
            intentCancel.putExtra("pathToOriginal", pathToOriginal)
            startActivity(intentCancel)
        }

        buttonDone.setOnClickListener {
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            val uriCurrentBitmap = bitmapToFile(currentBitmap)
            val intentDone = Intent(this, EditPhotoSecondScreenActivity::class.java)
            intentDone.putExtra("imagePath", uriCurrentBitmap)
            intentDone.putExtra("pathToOriginal", pathToOriginal)
            (application as IntermediateResults).functionCalls.addAll(listOf(6.0, scale))
            startActivity(intentDone)
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
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
}