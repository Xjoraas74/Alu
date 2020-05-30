package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_edit_photo.*
import kotlinx.android.synthetic.main.activity_edit_photo.buttonBack
import kotlinx.android.synthetic.main.activity_edit_photo.buttonEdit
import kotlinx.android.synthetic.main.activity_edit_photo.buttonSave
import kotlinx.android.synthetic.main.activity_edit_photo.imageToEdit
import kotlinx.android.synthetic.main.activity_edit_photo.progressBar
import kotlinx.coroutines.*
import java.io.*
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditPhotoActivity : AppCompatActivity() {

    companion object {
        const val redColor = Color.RED
        const val blueColor = Color.BLUE
        const val greenColor = Color.GREEN
        const val yellowColor = Color.YELLOW
        const val magentaColor = Color.MAGENTA
        const val cyanColor = Color.CYAN
    }

    var filter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        //получение фотографии
        val fileUri: Uri = intent.getParcelableExtra("imagePath")
        val pathToOriginal: Uri = intent.getParcelableExtra("pathToOriginal")

        println(fileUri)
        println(pathToOriginal)

        //конвертация полученного изображения в Bitmap в сжатой версии 1024*1024
        val bmpEditImage = decodeSampledBitmapFromFile(fileUri, 1024, 1024, this)

        println("${bmpEditImage?.width} ${bmpEditImage?.height}")

        imageToEdit.setImageBitmap(bmpEditImage)

        //скрытие progress bar'а
        progressBar.visibility = GONE

        //создание изображения на кнопках выбора фильтра
        var buttonChooseFilters = Bitmap.createBitmap(
            bmpEditImage!!.width,
            bmpEditImage.height,
            Bitmap.Config.ARGB_8888
        )
        buttonChooseFilters = decodeSampledBitmapFromFile(fileUri, 256, 256, this)

        buttonFilterThird.setImageBitmap((application as IntermediateResults).sepiaFilter(buttonChooseFilters))
        buttonNormalFilter.setImageBitmap(buttonChooseFilters)
        buttonFilterFirst.setImageBitmap((application as IntermediateResults).blackAndWhiteFilter(buttonChooseFilters))
        buttonFilterFourth.setImageBitmap((application as IntermediateResults).grayScaleFilter(buttonChooseFilters))
        buttonFilterSecond.setImageBitmap((application as IntermediateResults).negativeFilter(buttonChooseFilters))
        buttonFilterFifth.setImageBitmap((application as IntermediateResults).coloredFilter(buttonChooseFilters, redColor))
        buttonFilterSixth.setImageBitmap((application as IntermediateResults).coloredFilter(buttonChooseFilters, blueColor))
        buttonFilterSeventh.setImageBitmap((application as IntermediateResults).coloredFilter(buttonChooseFilters, greenColor))
        buttonFilterEighth.setImageBitmap((application as IntermediateResults).coloredFilter(buttonChooseFilters, yellowColor))
        buttonFilterNinth.setImageBitmap((application as IntermediateResults).coloredFilter(buttonChooseFilters, magentaColor))
        buttonFilterTenth.setImageBitmap((application as IntermediateResults).coloredFilter(buttonChooseFilters, cyanColor))

        //функционирование кнопок выбора фильтра
        buttonNormalFilter.setOnClickListener {
            imageToEdit.setImageBitmap(bmpEditImage)
            filter = 0
        }

        buttonFilterFirst.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).blackAndWhiteFilter(bmpEditImage))
            filter = 1
        }

        buttonFilterSecond.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).negativeFilter(bmpEditImage))
            filter = 4
        }

        buttonFilterThird.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).sepiaFilter(bmpEditImage))
            filter = 3
        }

        buttonFilterFourth.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).grayScaleFilter(bmpEditImage))
            filter = 2
        }

        buttonFilterFifth.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).coloredFilter(bmpEditImage, redColor))
            filter = 5 + redColor
        }

        buttonFilterSixth.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).coloredFilter(bmpEditImage, blueColor))
            filter = 5 + blueColor
        }

        buttonFilterSeventh.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).coloredFilter(bmpEditImage, greenColor))
            filter = 5 + greenColor
        }

        buttonFilterEighth.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).coloredFilter(bmpEditImage, yellowColor))
            filter = 5 + yellowColor
        }

        buttonFilterNinth.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).coloredFilter(bmpEditImage, magentaColor))
            filter = 5 + magentaColor
        }

        buttonFilterTenth.setOnClickListener {
            imageToEdit.setImageBitmap((application as IntermediateResults).coloredFilter(bmpEditImage, cyanColor))
            filter = 5 + cyanColor
        }

        //функционирование кнопки "Back"
        buttonBack.setOnClickListener {
            val backAlertDialog = AlertDialog.Builder(this)
            backAlertDialog.setIcon(R.drawable.ic_keyboard_backspace)
            backAlertDialog.setTitle("Выход")
            backAlertDialog.setMessage("Если вернуться в главное меню, изменения не будут сохранены")
            backAlertDialog.setPositiveButton("Назад") { _, _ ->
            }
            backAlertDialog.setNegativeButton("Сбросить изменения") { _, _ -> ProcessPhoenix.triggerRebirth(this)
            }
            backAlertDialog.show()
        }

        //функционирование кнопки "Редактировать" - нижнее меню
        buttonEdit.setOnClickListener {
            //получение изображения с применимыми фильтрами
            val bitmap = (imageToEdit.drawable as BitmapDrawable).bitmap
            (application as IntermediateResults).bitmapsList.add(bitmap)
            when (filter) {
                0 -> {}
                1 -> (application as IntermediateResults).functionCalls.add(1.0)
                2 -> (application as IntermediateResults).functionCalls.add(2.0)
                3 -> (application as IntermediateResults).functionCalls.add(3.0)
                4 -> (application as IntermediateResults).functionCalls.add(4.0)
                else -> (application as IntermediateResults).functionCalls.addAll(listOf(5.0, (filter - 5).toDouble()))
            }
            //передача изображения в другое активити
            val uriCurrentBitmap = bitmapToFile(bitmap)
            val i = Intent(this, EditPhotoSecondScreenActivity::class.java)

            i.putExtra("imagePath", uriCurrentBitmap)
            i.putExtra("pathToOriginal", pathToOriginal)

            println(uriCurrentBitmap)
            println(pathToOriginal)

            startActivity(i)
        }

        //функционирование кноки "Save"
        buttonSave.setOnClickListener {
            progressBar.visibility = VISIBLE

            when (filter) {
                0 -> {}
                1 -> (application as IntermediateResults).functionCalls.add(1.0)
                2 -> (application as IntermediateResults).functionCalls.add(2.0)
                3 -> (application as IntermediateResults).functionCalls.add(3.0)
                4 -> (application as IntermediateResults).functionCalls.add(4.0)
                else -> (application as IntermediateResults).functionCalls.addAll(listOf(5.0, (filter - 5).toDouble()))
            }

            println("onClickListener")
            CoroutineScope(Dispatchers.Default).launch {
                (application as IntermediateResults).save(pathToOriginal, this@EditPhotoActivity)
                println("launch 1")
                launch(Dispatchers.Main) {
                    println("launch 2")

                    val backAlertDialog = AlertDialog.Builder(this@EditPhotoActivity)
                    backAlertDialog.setIcon(R.drawable.ic_save)
                    backAlertDialog.setTitle("Сохранение")
                    backAlertDialog.setMessage("Фотография успешно сохранена")
                    backAlertDialog.setPositiveButton("Закрыть") { _, _ -> ProcessPhoenix.triggerRebirth(this@EditPhotoActivity) }
                    backAlertDialog.show()

                    progressBar.visibility = GONE
                }
            }
        }

    }

    //функция для получения Uri из Bitmap
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
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
        val bitmap = BitmapFactory.Options().run {
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

}