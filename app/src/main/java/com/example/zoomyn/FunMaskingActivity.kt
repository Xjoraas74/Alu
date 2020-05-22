package com.example.zoomyn

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlin.math.*
import kotlinx.android.synthetic.main.activity_edit_photo_second_screen.imageToEdit
import kotlinx.android.synthetic.main.activity_fun_masking.*
import kotlinx.android.synthetic.main.activity_fun_turn_arbitrary_angle.*

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

        //передаваемые параметры для функции маскирования
        var radius: Int = 0
        var threshold: Int = 0
        var amount: Int = 0

        //функционирование seekBar'а для радиуса
        seekBarMaskingRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingRadius.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    imageToEdit.setImageBitmap(unsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
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
                    imageToEdit.setImageBitmap(unsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

        //функционирование seekBar'а для эффекта
        seekBarMaskingAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressSeekBarMaskingAmount.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (currentBitmap != null) {
                    imageToEdit.setImageBitmap(unsharpMasking(currentBitmap, seekBarMaskingAmount.progress, seekBarMaskingRadius.progress, seekBarMaskingThreshold.progress))
                }
            }
        })

    }

    private fun unsharpMasking(orig: Bitmap, am: Int, rad: Int, thres: Int): Bitmap {
        val new = createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)

        // creates Gaussian distribution from row of Pascal's triangle excluding two elements on both ends
        val help = DoubleArray(rad * 2 + 5)
        help[0] = 1.0
        for (i in 1 until help.size) {
            help[i] = help[i - 1] * (help.size - i) / i
        }
        val gaussianDistribution = DoubleArray(help.size - 4)
        var sum: Double = 0.0
        for (i in  gaussianDistribution.indices) {
            gaussianDistribution[i] = help[i + 2]
            sum += gaussianDistribution[i]
        }
        for (i in gaussianDistribution.indices) {
            gaussianDistribution[i] = gaussianDistribution[i] / sum
        }

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

        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(new.width - 1, max(0, j + k))
                    convolvedHorizontallyRed += Color.red(pixelsOrig[i * orig.width + kAdjusted]) * gaussianDistribution[k]
                    convolvedHorizontallyGreen += Color.green(pixelsOrig[i * orig.width + kAdjusted]) * gaussianDistribution[k]
                    convolvedHorizontallyBlue += Color.blue(pixelsOrig[i * orig.width + kAdjusted]) * gaussianDistribution[k]
                }
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(new.height - 1, max(0, i + k))
                    convolvedVerticallyRed += Color.red(pixelsOrig[kAdjusted * orig.width + j]) * gaussianDistribution[k]
                    convolvedVerticallyGreen += Color.green(pixelsOrig[kAdjusted * orig.width + j]) * gaussianDistribution[k]
                    convolvedVerticallyBlue += Color.blue(pixelsOrig[kAdjusted * orig.width + j]) * gaussianDistribution[k]
                }
                red = Color.red(pixelsOrig[i * orig.width + j])
                green = Color.green(pixelsOrig[i * orig.width + j])
                blue = Color.blue(pixelsOrig[i * orig.width + j])
                redDiff = red - ((convolvedHorizontallyRed + convolvedVerticallyRed) / 2).roundToInt()
                greenDiff = green - ((convolvedHorizontallyGreen + convolvedVerticallyGreen) / 2).roundToInt()
                blueDiff = blue - ((convolvedHorizontallyBlue + convolvedVerticallyBlue) / 2).roundToInt()

                if (redDiff > thres) {
                    red += (redDiff * .01 * am).roundToInt()
                }
                if (greenDiff > thres) {
                    green += (greenDiff * .01 * am).roundToInt()
                }
                if (blueDiff > thres) {
                    blue += (blueDiff * .01 * am).roundToInt()
                }

                red = min(255, red)
                green = min(255, green)
                blue = min(255, blue)
                pixelsNew[i * new.width + j] = Color.rgb(red, green, blue)

                convolvedHorizontallyRed = 0.0
                convolvedHorizontallyGreen = 0.0
                convolvedHorizontallyBlue = 0.0
                convolvedVerticallyRed = 0.0
                convolvedVerticallyGreen = 0.0
                convolvedVerticallyBlue = 0.0
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

}
