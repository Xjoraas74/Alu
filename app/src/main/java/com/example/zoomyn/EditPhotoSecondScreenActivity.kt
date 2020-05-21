package com.example.zoomyn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.*

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
            val intentBack = Intent(this, EditPhotoActivity::class.java)
            startActivity(intentBack)
        }

        //функционирование кнопок выбора функций
        //поворот
        buttonTurn.setOnClickListener {
            val i = Intent(this, FunTurnActivity::class.java)
            i.putExtra("imagePath", fileUri.toString())
            startActivity(i)
        }
    }
}
