package com.example.zoomyn

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.graphics.ColorUtils
import kotlin.math.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_masking.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class FunMaskingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_masking)

        //извлечение изображения из предыдущей активити c примененными фильтрами
        val intent = intent
        val imagePath = intent.getStringExtra("imagePath")
        val fileUri = Uri.parse(imagePath)

        //показ полученной фотографии на экран
        imageToEdit.setImageURI(fileUri)

        //преобразование полученного изображения в Bitmap
        var currentBitmap = (imageToEdit.drawable as BitmapDrawable).bitmap

        //функционирование seekBar'а для радиуса
        seekBarMaskingRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingRadius.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    imageToEdit.setImageBitmap(callUnsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

        //функционирование seekBar'а для порога
        seekBarMaskingThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingThreshold.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    imageToEdit.setImageBitmap(callUnsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

        //функционирование seekBar'а для эффекта
        seekBarMaskingAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingAmount.text = "$i %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    imageToEdit.setImageBitmap(callUnsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

        //функционирование кнопок нижнего меню
        buttonCancel.setOnClickListener {
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

    private fun unsharpMasking(
        pixelsOrig: IntArray,
        pixelsNew: IntArray,
        am: Int,
        threshold: Float,
        gaussianDistribution: DoubleArray,
        iMin: Int,
        iMax:Int,
        bitmapWidth: Int,
        maxPossibleY: Int
    ) {
        var convolvedHorizontallyRed = 0.0
        var convolvedHorizontallyGreen = 0.0
        var convolvedHorizontallyBlue = 0.0
        var convolvedVerticallyRed = 0.0
        var convolvedVerticallyGreen = 0.0
        var convolvedVerticallyBlue = 0.0
        var redDiff: Int
        var greenDiff: Int
        var blueDiff: Int
        var red: Int
        var green: Int
        var blue: Int
        var kAdjusted: Int
        val hslDiff = FloatArray(3)
        val hslUsed = FloatArray(3)

        for (i in iMin..iMax) {
            for (j in 0 until bitmapWidth) {
                // don't make convolution for boundary pixels?
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(bitmapWidth - 1, max(0, j + k))
                    convolvedHorizontallyRed += Color.red(pixelsOrig[i * bitmapWidth + kAdjusted]) * gaussianDistribution[k]
                    convolvedHorizontallyGreen += Color.green(pixelsOrig[i * bitmapWidth + kAdjusted]) * gaussianDistribution[k]
                    convolvedHorizontallyBlue += Color.blue(pixelsOrig[i * bitmapWidth + kAdjusted]) * gaussianDistribution[k]
                }
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(maxPossibleY, max(0, i + k))
                    convolvedVerticallyRed += Color.red(pixelsOrig[kAdjusted * bitmapWidth + j]) * gaussianDistribution[k]
                    convolvedVerticallyGreen += Color.green(pixelsOrig[kAdjusted * bitmapWidth + j]) * gaussianDistribution[k]
                    convolvedVerticallyBlue += Color.blue(pixelsOrig[kAdjusted * bitmapWidth + j]) * gaussianDistribution[k]
                }
                red = Color.red(pixelsOrig[i * bitmapWidth + j])
                green = Color.green(pixelsOrig[i * bitmapWidth + j])
                blue = Color.blue(pixelsOrig[i * bitmapWidth + j])
                redDiff = red - ((convolvedHorizontallyRed + convolvedVerticallyRed) / 2).roundToInt()
                greenDiff = green - ((convolvedHorizontallyGreen + convolvedVerticallyGreen) / 2).roundToInt()
                blueDiff = blue - ((convolvedHorizontallyBlue + convolvedVerticallyBlue) / 2).roundToInt()

                ColorUtils.RGBToHSL(redDiff, greenDiff, blueDiff, hslDiff)
                if (hslDiff[2] > threshold) {
                    ColorUtils.RGBToHSL(red, green, blue, hslUsed)
                    hslUsed[2] += hslDiff[2] * .01.toFloat() * am
                    pixelsNew[i * bitmapWidth + j] = ColorUtils.HSLToColor(hslUsed)
                } else {
                    pixelsNew[i * bitmapWidth + j] = pixelsOrig[i * bitmapWidth + j]
                }

                convolvedHorizontallyRed = 0.0
                convolvedHorizontallyGreen = 0.0
                convolvedHorizontallyBlue = 0.0
                convolvedVerticallyRed = 0.0
                convolvedVerticallyGreen = 0.0
                convolvedVerticallyBlue = 0.0
            }
        }
    }

    fun callUnsharpMasking(orig: Bitmap, amount: Int, radius: Int, threshold: Int): Bitmap {
        // amount value is taken in percents
        val new = createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)

        // creates Gaussian distribution from row of Pascal's triangle
        val gaussianDistribution = DoubleArray(radius * 2 + 1)
        gaussianDistribution[0] = 1.0
        var sum = gaussianDistribution[0]
        for (i in 1 until gaussianDistribution.size) {
            gaussianDistribution[i] = gaussianDistribution[i - 1] * (gaussianDistribution.size - i) / i
            sum += gaussianDistribution[i]
        }
        for (i in gaussianDistribution.indices) {
            gaussianDistribution[i] = gaussianDistribution[i] / sum
        }

        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        runBlocking {
            coroutineScope {
                val n = 100 // amount of coroutines to launch
                IntRange(0, n - 1).map {
                    async(Dispatchers.Default) {
                        unsharpMasking(
                            pixelsOrig,
                            pixelsNew,
                            amount,
                            1.toFloat() / 255 * threshold,
                            gaussianDistribution,
                            it * new.height / n,
                            if (it != n - 1) { (it + 1) * new.height / n - 1 } else { new.height - 1 },
                            new.width,
                            new.height - 1
                        )
                    }
                }.awaitAll()
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }
}
