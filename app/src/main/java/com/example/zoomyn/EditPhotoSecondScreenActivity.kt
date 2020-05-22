package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.buttonBack
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.buttonFilter
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class EditPhotoSecondScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo_second_screen)

        //извлечение изображения из предыдущей активити c примененными фильтрами
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //функционирование кнопки "Back"
        buttonBack.setOnClickListener {
            val backAlertDialog = AlertDialog.Builder(this)
            backAlertDialog.setIcon(R.drawable.ic_keyboard_backspace)
            backAlertDialog.setTitle("Выход")
            backAlertDialog.setMessage("Если вернуться в главное меню, изменения не будут сохранены")
            backAlertDialog.setPositiveButton("Назад") { dialog, id ->
            }
            backAlertDialog.setNegativeButton("Сбросить изменения") { dialog, id ->
                val intentNegativeButton = Intent(this, MainActivity::class.java)
                startActivity(intentNegativeButton)
            }
            backAlertDialog.show()
        }

        //функционирование кнопки "Фильтр" - нижнее меню
        buttonFilter.setOnClickListener {
            //получение изображения с применимыми фильтрами
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val intentFilter = Intent(this, EditPhotoActivity::class.java)
            intentFilter.putExtra("imagePath", uriCurrentBitmap.toString())
            startActivity(intentFilter)
        }

        //функционирование кнопок выбора функций
        //поворот
        buttonTurn.setOnClickListener {
            val intentTurn = Intent(this, FunTurnActivity::class.java)
            intentTurn.putExtra("imagePath", fileUri.toString())
            startActivity(intentTurn)
        }
        //маскирование
        buttonMasking.setOnClickListener {
            val intentMasking = Intent(this, FunMaskingActivity::class.java)
            intentMasking.putExtra("imagePath", fileUri.toString())
            startActivity(intentMasking)
        }
        //масштабирование
        buttonScale.setOnClickListener {
            val intentScale = Intent(this, FunScaleActivity::class.java)
            intentScale.putExtra("imagePath", fileUri.toString())
            startActivity(intentScale)
        }

    }

    //функция для получения Uri из Bitmap
    private fun bitmapToFile(bitmap: Bitmap): Uri {
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
