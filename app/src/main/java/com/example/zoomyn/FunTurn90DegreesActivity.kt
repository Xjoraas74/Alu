package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_turn90_degrees.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class FunTurn90DegreesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_turn90_degrees)

        //извлечение изображения из предыдущей активити c примененными фильтрами
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование кнопки поворота изображения
        buttonClickForTurn.setOnClickListener {
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            imageToEdit.setImageBitmap(rotate90DegreesClockwise(currentBitmap))
        }

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
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    //функция поворота изображения на 90 градусов
    private fun rotate90DegreesClockwise(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.height, orig.width, Bitmap.Config.ARGB_8888)

        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)
        val pixelsCount = orig.width * orig.height
        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        // it just uses "new.setPixel(j, i, orig.getPixel(i, orig.height - 1 - j))" formula in linear arrays, maybe can be simplified
        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                pixelsNew[i * new.width + j] = pixelsOrig[pixelsCount - (j + 1) * orig.width + i]
            }
        }
        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)

        return new
    }
}
