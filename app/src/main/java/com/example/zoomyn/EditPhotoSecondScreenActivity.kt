package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditPhotoSecondScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo_second_screen)

        //получение фотографии
        val fileUri: Uri = intent.getParcelableExtra("imagePath")
        val pathToOriginal: Uri = intent.getParcelableExtra("pathToOriginal")

        println(fileUri)
        println(pathToOriginal)

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //скрытие progress bar'а
        progressBar.visibility = GONE

        //функционирование кнопки "Back"
        buttonBack.setOnClickListener {
            val backAlertDialog = AlertDialog.Builder(this)
            backAlertDialog.setIcon(R.drawable.ic_keyboard_backspace)
            backAlertDialog.setTitle("Выход")
            backAlertDialog.setMessage("Если вернуться в главное меню, изменения не будут сохранены")
            backAlertDialog.setPositiveButton("Назад") { dialog, id ->
            }
            backAlertDialog.setNegativeButton("Сбросить изменения") { _, _ -> ProcessPhoenix.triggerRebirth(this)
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

            intentFilter.putExtra("imagePath", uriCurrentBitmap)
            intentFilter.putExtra("pathToOriginal", pathToOriginal)

            startActivity(intentFilter)
        }

        //функционирование кнопок выбора функций
        //поворот
        buttonTurn.setOnClickListener {
            //получение изображения с ImageView
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val intentTurn = Intent(this, FunTurnActivity::class.java)
            intentTurn.putExtra("imagePath", uriCurrentBitmap)
            intentTurn.putExtra("pathToOriginal", pathToOriginal)
            startActivity(intentTurn)
        }

        //маскирование
        buttonMasking.setOnClickListener {
            //получение изображения с ImageView
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val intentMasking = Intent(this, FunMaskingActivity::class.java)
            intentMasking.putExtra("imagePath", uriCurrentBitmap)
            intentMasking.putExtra("pathToOriginal", pathToOriginal)
            startActivity(intentMasking)
        }

        //масштабирование
        buttonScale.setOnClickListener {
            //получение изображения с ImageView
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val intentScale = Intent(this, FunScaleActivity::class.java)
            intentScale.putExtra("imagePath", uriCurrentBitmap)
            intentScale.putExtra("pathToOriginal", pathToOriginal)
            startActivity(intentScale)
        }

        //функционирование кнопки "Save"
        buttonSave.setOnClickListener {
            progressBar.visibility = VISIBLE
            CoroutineScope(Dispatchers.Default).launch {
                (application as IntermediateResults).save(pathToOriginal, this@EditPhotoSecondScreenActivity)
                println("launch 1")
                launch(Dispatchers.Main) {
                    println("launch 2")

                    val backAlertDialog = AlertDialog.Builder(this@EditPhotoSecondScreenActivity)
                    backAlertDialog.setIcon(R.drawable.ic_save)
                    backAlertDialog.setTitle("Сохранение")
                    backAlertDialog.setMessage("Фотография успешно сохранена")
                    backAlertDialog.setPositiveButton("Закрыть") { _, _ -> ProcessPhoenix.triggerRebirth(this@EditPhotoSecondScreenActivity) }
                    backAlertDialog.show()

                    progressBar.visibility = GONE
                }
            }
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