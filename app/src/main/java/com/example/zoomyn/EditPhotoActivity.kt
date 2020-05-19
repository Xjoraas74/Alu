package com.example.zoomyn

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo.*
import kotlin.math.min
import kotlin.math.roundToInt

class EditPhotoActivity : AppCompatActivity() {

    companion object {
        const val redColor = Color.RED
        const val blueColor = Color.BLUE
        const val greenColor = Color.GREEN
        const val yellowColor = Color.YELLOW
        const val magentaColor = Color.MAGENTA
        const val cyanColor = Color.CYAN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        //получение фотографии
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)
        imageToEdit.setImageURI(fileUri)

        //конвертация полученного изображения в Bitmap
        var bmpEditImage = MediaStore.Images.Media.getBitmap(this.contentResolver, fileUri)

        //создание изображения на кнопках выбора фильтра
        var buttonChooseFilters = Bitmap.createBitmap(bmpEditImage.width, bmpEditImage.height, Bitmap.Config.ARGB_8888)

        buttonChooseFilters = blackAndWhiteFilter(bmpEditImage)
        buttonFilterFirst.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = negativeFilter(bmpEditImage)
        buttonFilterSecond.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = sepiaFilter(bmpEditImage)
        buttonFilterThird.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = grayScaleFilter(bmpEditImage)
        buttonFilterFourth.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = coloredFilter(bmpEditImage, redColor)
        buttonFilterFifth.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = coloredFilter(bmpEditImage, blueColor)
        buttonFilterSixth.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = coloredFilter(bmpEditImage, greenColor)
        buttonFilterSeventh.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = coloredFilter(bmpEditImage, yellowColor)
        buttonFilterEighth.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = coloredFilter(bmpEditImage, magentaColor)
        buttonFilterNinth.setImageBitmap(buttonChooseFilters)

        buttonChooseFilters = coloredFilter(bmpEditImage, cyanColor)
        buttonFilterTenth.setImageBitmap(buttonChooseFilters)

        //функционирование кнопок выбора фильтра
        buttonFilterFirst.setOnClickListener {
            imageToEdit.setImageBitmap(blackAndWhiteFilter(bmpEditImage))
        }

        buttonFilterSecond.setOnClickListener {
            imageToEdit.setImageBitmap(negativeFilter(bmpEditImage))
        }

        buttonFilterThird.setOnClickListener {
            imageToEdit.setImageBitmap(sepiaFilter(bmpEditImage))
        }

        buttonFilterFourth.setOnClickListener {
            imageToEdit.setImageBitmap(grayScaleFilter(bmpEditImage))
        }

        buttonFilterFifth.setOnClickListener {
            imageToEdit.setImageBitmap(coloredFilter(bmpEditImage, redColor))
        }

        buttonFilterSixth.setOnClickListener {
            imageToEdit.setImageBitmap(coloredFilter(bmpEditImage, blueColor))
        }

        buttonFilterSeventh.setOnClickListener {
            imageToEdit.setImageBitmap(coloredFilter(bmpEditImage, greenColor))
        }

        buttonFilterEighth.setOnClickListener {
            imageToEdit.setImageBitmap(coloredFilter(bmpEditImage, yellowColor))
        }

        buttonFilterNinth.setOnClickListener {
            imageToEdit.setImageBitmap(coloredFilter(bmpEditImage, magentaColor))
        }

        buttonFilterTenth.setOnClickListener {
            imageToEdit.setImageBitmap(coloredFilter(bmpEditImage, cyanColor))
        }

        //функционирование кнопок верхнего меню
        buttonBack.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        //функционирование кнопки "Редактировать" - нижнее меню
        buttonEdit.setOnClickListener {
            val intent = Intent(this, EditPhotoSecondScreenActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setPixelsWithLookupTable(orig: Bitmap, new: Bitmap, table: IntArray) {
        val pixels = IntArray((orig.width) * (orig.height))
        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)
        for (i in pixels.indices) {
            pixels[i] = table[pixels[i] and 0xffffff] or 0xff000000.toInt()
        }
        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
    }

    //цветокоррекция и цветовые фильтры
    //чёрно-белый фильтр
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

    //фильтр "Негатив"
    private fun negativeFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val lookupTable = IntArray(0x1000000)
        /*
        R = 255 – R
        G = 255 – G
        B = 255 – B
        */
        for (i in 0 until 0x1000000) {
            lookupTable[i] = Color.rgb(255 - Color.red(i), 255 - Color.green(i), 255 - Color.blue(i))
        }
        setPixelsWithLookupTable(orig, new, lookupTable)
        return new
    }

    //фильтр "сепия"
    private fun sepiaFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val lookupTable = IntArray(0x1000000)
        /*
        outputRed = (inputRed * .393) + (inputGreen *.769) + (inputBlue * .189)
        outputGreen = (inputRed * .349) + (inputGreen *.686) + (inputBlue * .168)
        outputBlue = (inputRed * .272) + (inputGreen *.534) + (inputBlue * .131)

        if greater than 255, round to 255
        */
        for (i in 0 until 0x1000000) {
            lookupTable[i] = Color.rgb(
                min(255, (0.393 * Color.red(i) + 0.769 * Color.green(i) + 0.189 * Color.blue(i)).roundToInt()),
                min(255, (0.349 * Color.red(i) + 0.686 * Color.green(i) + 0.168 * Color.blue(i)).roundToInt()),
                min(255, (0.272 * Color.red(i) + 0.534 * Color.green(i) + 0.131 * Color.blue(i)).roundToInt())
            )
        }

        setPixelsWithLookupTable(orig, new, lookupTable)
        return new
    }

    //чёрно-белый с оттенками серого
    private fun grayScaleFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        var gray: Int
        val lookupTable = IntArray(0x1000000)

        // Gray = (Red * 0.3 + Green * 0.59 + Blue * 0.11)
        for (i in 0 until 0x1000000) {
            gray=(Color.red(i) * 0.3 + Color.green(i) * 0.59 + Color.blue(i) * 0.11).roundToInt()
            lookupTable[i] = Color.rgb(gray, gray, gray)
        }

        setPixelsWithLookupTable(orig, new, lookupTable)
        return new
    }

    //цветной фильтр
    private fun coloredFilter(orig: Bitmap, col: Int): Bitmap {
        val new = createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val lookupTable = IntArray(0x1000000)

        for (i in 0 until 0x1000000) {
            lookupTable[i] = col and i
        }

        setPixelsWithLookupTable(orig, new, lookupTable)
        return new
    }
}
