package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_turn.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FunTurnActivity : AppCompatActivity() {

    var count = 0
    var angle = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_turn)

        //получение фотографии
        val fileUri: Uri = intent.getParcelableExtra("imagePath")
        val pathToOriginal: Uri = intent.getParcelableExtra("pathToOriginal")

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
        val cancelBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование кнопки поворота изображения на 90
        buttonRotate90.setOnClickListener {
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            imageToEdit.setImageBitmap((application as IntermediateResults).callRotate90DegreesClockwise(currentBitmap))
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            count++
        }

        //функционирование seekBar'а для поворота иображения на произвольный угол
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
            for (i in 1..count % 4) {
                (application as IntermediateResults).functionCalls.add(7.0)
            }
            (application as IntermediateResults).functionCalls.addAll(listOf(8.0, angle.toDouble()))
            startActivity(intentDone)
        }

    }

    //функция для получения Uri из Bitmap
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }
}