package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*
import kotlinx.coroutines.*
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
        val pathToOriginal = Uri.parse(intent.getStringExtra("pathToOriginal"))

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //скрытие progress bar'а
        progressBar.visibility = View.GONE

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
            intentTurn.putExtra("pathToOriginal", pathToOriginal.toString())
            startActivity(intentTurn)
        }
        //маскирование
        buttonMasking.setOnClickListener {
            val intentMasking = Intent(this, FunMaskingActivity::class.java)
            intentMasking.putExtra("imagePath", fileUri.toString())
            intentMasking.putExtra("pathToOriginal", pathToOriginal.toString())
            startActivity(intentMasking)
        }
        //масштабирование
        buttonScale.setOnClickListener {
            val intentScale = Intent(this, FunScaleActivity::class.java)
            intentScale.putExtra("imagePath", fileUri.toString())
            intentScale.putExtra("pathToOriginal", pathToOriginal.toString())
            startActivity(intentScale)
        }

        buttonSave.setOnClickListener {
            runBlocking {
                val saving = CoroutineScope(Dispatchers.Default).async {
                    (application as IntermediateResults).save(pathToOriginal, this@EditPhotoSecondScreenActivity)
                }

                //progress bar
                progressBar.visibility = View.VISIBLE
                println("shown")

                //await finish saving, close progress bar, finish activity
                saving.await()
                progressBar.visibility = View.GONE
                println("gone")
                val backAlertDialog = AlertDialog.Builder(this@EditPhotoSecondScreenActivity)
                backAlertDialog.setIcon(R.drawable.ic_save)
                backAlertDialog.setTitle("Выход")
                backAlertDialog.setMessage("Фотография успешно сохранена")
                backAlertDialog.setPositiveButton("Закрыть") { _, _ -> }
                backAlertDialog.show()
                Thread.sleep(1000)
                progressBar.visibility = View.GONE
            }
        }

        textCancel.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).undo((imageToEdit.drawable as BitmapDrawable).bitmap))
            println((application as IntermediateResults).functionCalls)
        }

        buttonUndo.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).undo((imageToEdit.drawable as BitmapDrawable).bitmap))
            println((application as IntermediateResults).functionCalls)
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

}
