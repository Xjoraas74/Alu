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
                textSeekBarScale.text = "$i°"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    val value = seekBarScale.progress.toDouble()
                    imageToEdit.setImageBitmap(scale(currentBitmap, value))
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

    private fun getPixelsBilinearlyScaled(orig: IntArray, scaleFactor: Double, origWidth: Int, origHeight: Int): IntArray {
        val nw = (origWidth * scaleFactor).roundToInt()
        val nh = (origHeight * scaleFactor).roundToInt()
        val pixelsNew = IntArray(nw * nh)

        var iFloorX: Int
        var iCeilingX: Int
        var iFloorY: Int
        var iCeilingY: Int
        var fTrueX: Double
        var fTrueY: Double
        var fDeltaX: Double
        var fDeltaY: Double
        var clrTopLeft: Int
        var clrTopRight: Int
        var clrBottomLeft: Int
        var clrBottomRight: Int
        var fTopRed: Double
        var fTopGreen: Double
        var fTopBlue: Double
        var fBottomRed: Double
        var fBottomGreen: Double
        var fBottomBlue: Double
        var iRed: Int
        var iGreen: Int
        var iBlue: Int

        for (i in 0 until nh) {
            for (j in 0 until nw) {
                fTrueX = j / scaleFactor
                fTrueY = i / scaleFactor

                iFloorX = kotlin.math.floor(fTrueX).toInt()
                iFloorY = kotlin.math.floor(fTrueY).toInt()
                iCeilingX = kotlin.math.ceil(fTrueX).toInt()
                iCeilingY = kotlin.math.ceil(fTrueY).toInt()
                if (iFloorX < 0 || iCeilingX >= origWidth || iFloorY < 0 || iCeilingY >= origHeight) continue

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * origWidth + iFloorX
                clrTopRight = iFloorY * origWidth + iCeilingX
                clrBottomLeft = iCeilingY * origWidth + iFloorX
                clrBottomRight = iCeilingY * origWidth + iCeilingX

                // linearly interpolate horizontally between top neighbours
                fTopRed = (1 - fDeltaX) * Color.red(orig[clrTopLeft]) + fDeltaX * Color.red(orig[clrTopRight])
                fTopGreen = (1 - fDeltaX) * Color.green(orig[clrTopLeft]) + fDeltaX * Color.green(orig[clrTopRight])
                fTopBlue = (1 - fDeltaX) * Color.blue(orig[clrTopLeft]) + fDeltaX * Color.blue(orig[clrTopRight])

                // linearly interpolate horizontally between bottom neighbours
                fBottomRed = (1 - fDeltaX) * Color.red(orig[clrBottomLeft]) + fDeltaX * Color.red(orig[clrBottomRight])
                fBottomGreen = (1 - fDeltaX) * Color.green(orig[clrBottomLeft]) + fDeltaX * Color.green(orig[clrBottomRight])
                fBottomBlue = (1 - fDeltaX) * Color.blue(orig[clrBottomLeft]) + fDeltaX * Color.blue(orig[clrBottomRight])

                // linearly interpolate vertically between top and bottom interpolated results
                iRed = ((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed).roundToInt()
                iGreen = ((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen).roundToInt()
                iBlue = ((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue).roundToInt()

                pixelsNew[i * nw + j] = Color.rgb(iRed, iGreen, iBlue)
            }
        }

        return pixelsNew
    }

    //функция масштабирования полученного изображения
    private fun scale(orig: Bitmap, scaleFactor: Double): Bitmap {
        if ((orig.width * scaleFactor).roundToInt() < 1 || (orig.height * scaleFactor).roundToInt() < 1) {
            val new = createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            Toast.makeText(this, "The value is too small; can't resize!", Toast.LENGTH_LONG).show()
            return new
        }

        if (scaleFactor == 1.0) {
            return orig
        }

        val new = createBitmap(
            (orig.width * scaleFactor).roundToInt(),
            (orig.height * scaleFactor).roundToInt(),
            Bitmap.Config.ARGB_8888
        )

        val pixels = IntArray(orig.width * orig.height)
        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)
        new.setPixels(
            getPixelsBilinearlyScaled(pixels, scaleFactor, orig.width, orig.height),
            0,
            new.width,
            0,
            0,
            new.width,
            new.height
        )
        return new
    }

}
