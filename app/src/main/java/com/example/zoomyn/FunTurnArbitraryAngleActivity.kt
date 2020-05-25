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
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.math.*

class FunTurnArbitraryAngle : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_turn_arbitrary_angle)

        //извлечение изображения из предыдущей активити c примененными фильтрами
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование seekBar'а
        seekBarTurn.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textSeekBarTurn.text = "Поворот на : $i°"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    val angle = seekBarTurn.progress
                    imageToEdit.setImageBitmap(rotateClockwiseByDegrees(currentBitmap, angle))
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

    fun rotateClockwiseByDegrees(orig: Bitmap, aDeg: Int): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val a = aDeg * PI / 180
        val iCentreX = new.width / 2
        val iCentreY = new.height / 2
        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)

        var x: Int
        var y: Int
        var fDistance: Double
        var fPolarAngle: Double
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

        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                x = j - iCentreX
                y = iCentreY - i

                fDistance = sqrt((x * x + y * y).toDouble())
                fPolarAngle = atan2(y.toDouble(), x.toDouble())
                fPolarAngle += a

                fTrueX = fDistance * cos(fPolarAngle)
                fTrueY = fDistance * sin(fPolarAngle)

                fTrueX = fTrueX + iCentreX
                fTrueY = iCentreY - fTrueY

                iFloorX = floor(fTrueX).toInt()
                iFloorY = floor(fTrueY).toInt()
                iCeilingX = ceil(fTrueX).toInt()
                iCeilingY = ceil(fTrueY).toInt()
                if (iFloorX < 0 || iCeilingX >= orig.width || iFloorY < 0 || iCeilingY >= orig.height) {
                    pixelsNew[i * new.width + j] = Color.WHITE
                    continue
                }

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * orig.width + iFloorX
                clrTopRight = iFloorY * orig.width + iCeilingX
                clrBottomLeft = iCeilingY * orig.width + iFloorX
                clrBottomRight = iCeilingY * orig.width + iCeilingX

                // linearly interpolate horizontally between top neighbours
                fTopRed = (1 - fDeltaX) * Color.red(pixelsOrig[clrTopLeft]) + fDeltaX * Color.red(pixelsOrig[clrTopRight])
                fTopGreen = (1 - fDeltaX) * Color.green(pixelsOrig[clrTopLeft]) + fDeltaX * Color.green(pixelsOrig[clrTopRight])
                fTopBlue = (1 - fDeltaX) * Color.blue(pixelsOrig[clrTopLeft]) + fDeltaX * Color.blue(pixelsOrig[clrTopRight])

                // linearly interpolate horizontally between bottom neighbours
                fBottomRed = (1 - fDeltaX) * Color.red(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.red(pixelsOrig[clrBottomRight])
                fBottomGreen = (1 - fDeltaX) * Color.green(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.green(pixelsOrig[clrBottomRight])
                fBottomBlue = (1 - fDeltaX) * Color.blue(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.blue(pixelsOrig[clrBottomRight])

                // linearly interpolate vertically between top and bottom interpolated results
                iRed = ((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed).roundToInt()
                iGreen = ((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen).roundToInt()
                iBlue = ((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue).roundToInt()

                pixelsNew[i * new.width + j] = Color.rgb(iRed, iGreen, iBlue)
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

}
