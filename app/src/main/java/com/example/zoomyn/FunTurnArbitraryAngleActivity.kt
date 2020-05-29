package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.math.*
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.*
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.imageToEdit

class FunTurnArbitraryAngle : AppCompatActivity() {
    var angle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_turn_arbitrary_angle)

        //извлечение изображения из предыдущей активити c примененными фильтрами
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)
        val pathToOriginal = Uri.parse(intent.getStringExtra("pathToOriginal"))
        println(fileUri)
        println("$pathToOriginal done")

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование seekBar'а
        seekBarTurn.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textSeekBarTurn.text = "Поворот на : ${i - 45}°"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    angle = seekBarTurn.progress - 45
                    imageToEdit.setImageBitmap((application as IntermediateResults).rotateClockwiseByDegrees(currentBitmap, angle))
                }
            }
        })

        //функционирование кнопок нижнего меню
        buttonCancel.setOnClickListener {
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(currentBitmap)
            val i = Intent(this, FunTurnActivity::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            startActivity(i)
        }

        buttonDone.setOnClickListener {
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            val uriCurrentBitmap = bitmapToFile(currentBitmap)
            val i = Intent(this, EditPhotoSecondScreenActivity::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            i.putExtra("pathToOriginal", pathToOriginal.toString())
            println(angle.toDouble())
            (application as IntermediateResults).functionCalls.addAll(listOf(8.0, angle.toDouble()))
            startActivity(i)
        }
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
