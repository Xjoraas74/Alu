package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.graphics.ColorUtils
import kotlin.math.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.view.*
import kotlinx.android.synthetic.main.activity_fun_masking.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FunMaskingActivity : AppCompatActivity() {

    var amount = 0
    var radius = 0
    var threshold = 0
    var ratioToOriginal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_masking)

        //получение фотографии
        val fileUri: Uri = intent.getParcelableExtra("imagePath")
        val pathToOriginal: Uri = intent.getParcelableExtra("pathToOriginal")

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
        val cancelBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование seekBar'а для радиуса
        seekBarMaskingRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingRadius.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    radius = seekBarMaskingRadius.progress
                    imageToEdit.setImageBitmap((application as IntermediateResults).callUnsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

        //функционирование seekBar'а для порога
        seekBarMaskingThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingThreshold.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    threshold = seekBarMaskingThreshold.progress
                    imageToEdit.setImageBitmap((application as IntermediateResults).callUnsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

        //функционирование seekBar'а для эффекта
        seekBarMaskingAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingAmount.text = "$i%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    amount = seekBarMaskingAmount.progress
                    imageToEdit.setImageBitmap((application as IntermediateResults).callUnsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
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
            (application as IntermediateResults).functionCalls.addAll(
                listOf(9.0, amount.toDouble(), (radius / ratioToOriginal).roundToInt().toDouble(), threshold.toDouble())
            )
            startActivity(intentDone)
        }
        ratioToOriginal = findRatio(pathToOriginal, this, (imageToEdit.drawable as BitmapDrawable).bitmap.height)

    }

    //функция для получения Uri из Bitmap
    private fun bitmapToFile(bitmap:Bitmap): Uri {
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

    private fun findRatio(uri: Uri, context: Context, heightShown: Int): Double {
        BitmapFactory.Options().run {
            val s = context.contentResolver.openInputStream(uri)
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(s, null, this)
            return heightShown.toDouble() / outHeight
        }
    }
}