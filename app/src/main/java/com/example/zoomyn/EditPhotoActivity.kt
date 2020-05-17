package com.example.zoomyn

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo.*
import kotlin.math.roundToInt

class EditPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        //получение фотографии
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        var fileUri = Uri.parse(imagePath)
        imageToEdit.setImageURI(fileUri)

        //присваивание компонентам ImageView полученную фотографию
        buttonFilterFirst.setImageURI(fileUri)
        buttonFilterSecond.setImageURI(fileUri)
        buttonFilterThird.setImageURI(fileUri)
        buttonFilterFourth.setImageURI(fileUri)
        buttonFilterFifth.setImageURI(fileUri)
        buttonFilterSixth.setImageURI(fileUri)
        buttonFilterSeventh.setImageURI(fileUri)
        buttonFilterEighth.setImageURI(fileUri)
        buttonFilterEighth.setImageURI(fileUri)
        buttonFilterNinth.setImageURI(fileUri)
        buttonFilterTenth.setImageURI(fileUri)

        //функционирование кнопок верхнего меню
        buttonBack.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        //функционирование кнопки "Редактировать"
        buttonEdit.setOnClickListener {
            val intent = Intent(this, EditPhotoSecondScreenActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setPixelsWithLookupTable(orig: Bitmap, new: Bitmap, table: IntArray) {
        for (x in 0 until new.width) {
            for (y in 0 until new.height) {
                new.setPixel(x, y, table[orig.getPixel(x, y) and 0xffffff] or 0xff000000.toInt())
            }
        }
    }

    private fun blackAndWhiteFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        var gray: Int
        val lookupTable = IntArray(0x1000000)

        // Gray = (Red * 0.3 + Green * 0.59 + Blue * 0.11)
        for (i in 0 until 0x1000000) {
            gray=(Color.red(i) * 0.3 + Color.green(i) * 0.59 + Color.blue(i) * 0.11).roundToInt()
            lookupTable[i] = if (gray <= 127) {
                Color.BLACK
            } else {
                Color.WHITE
            }
        }
        setPixelsWithLookupTable(orig, new, lookupTable)
        return new
    }


}
