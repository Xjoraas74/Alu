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
import kotlinx.android.synthetic.main.activity_fun_turn.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class FunTurnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_turn)

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

        //функционирование кнопок режима выбора поворота
        button90degrees.setOnClickListener {
            //получение изображения с применимыми фильтрами
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val i = Intent(this, FunTurn90DegreesActivity::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            startActivity(i)
        }

        buttonArbitraryAngle.setOnClickListener {
            //получение изображения с применимыми фильтрами
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val i = Intent(this, FunTurnArbitraryAngle::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            i.putExtra("pathToOriginal", pathToOriginal.toString())
            startActivity(i)
        }

        //функционирование кнопок нижнего меню
        buttonCancel.setOnClickListener {
            currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
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

}
