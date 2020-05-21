package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_photo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.*
import java.util.*
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

        //конвертация полученного изображения в Bitmap в сжатой версии 1024*1024
        var bmpEditImage = decodeSampledBitmapFromFile(fileUri, 1024, 1024, this)
        imageToEdit.setImageBitmap(bmpEditImage)

        //создание изображения на кнопках выбора фильтра
        var buttonChooseFilters = Bitmap.createBitmap(bmpEditImage!!.width, bmpEditImage.height, Bitmap.Config.ARGB_8888)
        buttonChooseFilters = decodeSampledBitmapFromFile(fileUri, 256, 256, this)

        CoroutineScope(Dispatchers.Default).launch {
            buttonFilterThird.setImageBitmap(sepiaFilter(buttonChooseFilters))
        }
        buttonNormalFilter.setImageBitmap(buttonChooseFilters)
        buttonFilterFirst.setImageBitmap(blackAndWhiteFilter(buttonChooseFilters))
        buttonFilterFourth.setImageBitmap(grayScaleFilter(buttonChooseFilters))
        buttonFilterSecond.setImageBitmap(negativeFilter(buttonChooseFilters))
        buttonFilterFifth.setImageBitmap(coloredFilter(buttonChooseFilters, redColor))
        buttonFilterSixth.setImageBitmap(coloredFilter(buttonChooseFilters, blueColor))
        buttonFilterSeventh.setImageBitmap(coloredFilter(buttonChooseFilters, greenColor))
        buttonFilterEighth.setImageBitmap(coloredFilter(buttonChooseFilters, yellowColor))
        buttonFilterNinth.setImageBitmap(coloredFilter(buttonChooseFilters, magentaColor))
        buttonFilterTenth.setImageBitmap(coloredFilter(buttonChooseFilters, cyanColor))

        //функционирование кнопок выбора фильтра
        buttonNormalFilter.setOnClickListener {
            imageToEdit.setImageBitmap(bmpEditImage)
        }

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

        //функционирование кнопки "Редактировать" - нижнее меню
        buttonEdit.setOnClickListener {
            //получение изображения с применимыми фильтрами
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val i = Intent(this, EditPhotoSecondScreenActivity::class.java)
            i.putExtra("imagePath", uriCurrentBitmap.toString())
            startActivity(i)
        }
    }

    //функция для получения Uri из Bitmap
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
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

    //сжатие фотографии
    //метод для вычисления новых размеров изображения по заданными ширине и высоте
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        //ширина и длина исходного изображения
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height
            val halfWidth: Int = width

            //рассчитываем отношение высоты и ширины к требуемой высоте и ширине
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    //основной метод декодирование исходного изображения
    private fun decodeSampledBitmapFromFile(curUri: Uri, reqWidth: Int, reqHeight: Int, context: Context): Bitmap? {
        //сначала декодируем с помощью inJustDecodeBounds=true для проверки размеров
        var bitmap = BitmapFactory.Options().run {
            val stream = context.contentResolver.openInputStream(curUri)
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(stream, null, this)

            //посчитаем inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight);

            //декодирование растрового изображения с помощью набора inSampleSize
            inJustDecodeBounds = false;
            val newBitmap = context.contentResolver.openInputStream(curUri)
            BitmapFactory.decodeStream(newBitmap, null, this)
        }
        return bitmap
    }

    //цветокоррекция и цветовые фильтры
    //фильтр "негатив"
    private fun negativeFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)
        /*
        R = 255 – R
        G = 255 – G
        B = 255 – B
        */
        for (i in pixels.indices) {
            pixels[i] = Color.rgb(255 - Color.red(pixels[i]), 255 - Color.green(pixels[i]), 255 - Color.blue(pixels[i]))
        }

        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    //фильтр "сепия"
    private fun sepiaFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)
        /*
        outputRed = (inputRed * .393) + (inputGreen *.769) + (inputBlue * .189)
        outputGreen = (inputRed * .349) + (inputGreen *.686) + (inputBlue * .168)
        outputBlue = (inputRed * .272) + (inputGreen *.534) + (inputBlue * .131)

        if greater than 255, round to 255
        */
        for (i in pixels.indices) {
            pixels[i] = Color.rgb(
                min(255, (0.393 * Color.red(pixels[i]) + 0.769 * Color.green(pixels[i]) + 0.189 * Color.blue(pixels[i])).roundToInt()),
                min(255, (0.349 * Color.red(pixels[i]) + 0.686 * Color.green(pixels[i]) + 0.168 * Color.blue(pixels[i])).roundToInt()),
                min(255, (0.272 * Color.red(pixels[i]) + 0.534 * Color.green(pixels[i]) + 0.131 * Color.blue(pixels[i])).roundToInt())
            )
        }

        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    //чёрно-белый с оттенками серого
    private fun grayScaleFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)

        // Gray = (Red * 0.3 + Green * 0.59 + Blue * 0.11)
        var gray: Int
        for (i in pixels.indices) {
            gray = (Color.red(pixels[i]) * 0.3 + Color.green(pixels[i]) * 0.59 + Color.blue(pixels[i]) * 0.11).roundToInt()
            pixels[i] = Color.rgb(gray, gray, gray)
        }

        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    //чёрно-белый фильтр
    private fun blackAndWhiteFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)

        // Gray = (Red * 0.3 + Green * 0.59 + Blue * 0.11)
        var gray: Int
        for (i in pixels.indices) {
            gray = (Color.red(pixels[i]) * 0.3 + Color.green(pixels[i]) * 0.59 + Color.blue(pixels[i]) * 0.11).roundToInt()
            pixels[i] = if (gray <= 127) {
                Color.BLACK
            } else {
                Color.WHITE
            }
        }

        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    //цветной фильтр
    private fun coloredFilter(orig: Bitmap, col: Int): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)

        for (i in pixels.indices) {
           pixels[i] = col and pixels[i]
        }

        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
        return new
    }
}
