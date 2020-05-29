package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.buttonBack
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.buttonFilter
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.buttonSave
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
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
        println("$pathToOriginal done")

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //скрытие progress bar'а
        val progressBar = findViewById<ProgressBar>(R.id.progressBar) as ProgressBar
        progressBar.visibility = View.INVISIBLE

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
            //получение изображения
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

        //функционирование кнопки сохранения
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
                progressBar.visibility = View.GONE
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
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

}
