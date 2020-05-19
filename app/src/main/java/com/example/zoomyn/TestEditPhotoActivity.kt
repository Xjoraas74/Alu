package com.example.zoomyn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_test_edit_photo.*
import kotlin.math.*

class TestEditPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_edit_photo)

        button.setOnClickListener {
            apply(imageViewOrig, imageViewNew)
        }
    }

    private fun rotate90DegreesClockwise(orig: Bitmap): Bitmap {
        val new = createBitmap(orig.height, orig.width, Bitmap.Config.ARGB_8888)

        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)
        val pixelsCount = orig.width * orig.height
        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        // it just uses "new.setPixel(j, i, orig.getPixel(i, orig.height - 1 - j))" formula in linear arrays, maybe can be simplified
        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                pixelsNew[i * new.width + j] = pixelsOrig[pixelsCount - (j + 1) * orig.width + i]
            }
        }
        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)

        return new
    }

    private fun rotateClockwiseByDegrees(orig: Bitmap, aDeg: Int): Bitmap {
        val new = createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val a = aDeg * PI / 180
        val iCentreX = new.width / 2
        val iCentreY = new.height / 2
        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)

        var x: Int
        var y: Int
        var fDistance: Double
        var fPolarAngle: Double
        var iFloorX: Int
        var iCeilingX: Int
        var iFloorY: Int
        var iCeilingY: Int
        var fTrueX: Double
        var fTrueY: Double
        var fDeltaX: Double
        var fDeltaY: Double
        var clrTopLeft: Int
        var clrTopRight: Int
        var clrBottomLeft: Int
        var clrBottomRight: Int
        var fTopRed: Double
        var fTopGreen: Double
        var fTopBlue: Double
        var fBottomRed: Double
        var fBottomGreen: Double
        var fBottomBlue: Double
        var iRed: Int
        var iGreen: Int
        var iBlue: Int

        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                x = j - iCentreX
                y = iCentreY - i

                fDistance = sqrt((x * x + y * y).toDouble())
                fPolarAngle = atan2(y.toDouble(), x.toDouble())
                fPolarAngle += a

                fTrueX = fDistance * cos(fPolarAngle)
                fTrueY = fDistance * sin(fPolarAngle)

                fTrueX = fTrueX + iCentreX
                fTrueY = iCentreY - fTrueY

                iFloorX = floor(fTrueX).toInt()
                iFloorY = floor(fTrueY).toInt()
                iCeilingX = ceil(fTrueX).toInt()
                iCeilingY = ceil(fTrueY).toInt()
                if (iFloorX < 0 || iCeilingX >= orig.width || iFloorY < 0 || iCeilingY >= orig.height) continue

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * orig.width + iFloorX
                clrTopRight = iFloorY * orig.width + iCeilingX
                clrBottomLeft = iCeilingY * orig.width + iFloorX
                clrBottomRight = iCeilingY * orig.width + iCeilingX

                // linearly interpolate horizontally between top neighbours
                fTopRed = (1 - fDeltaX) * Color.red(pixelsOrig[clrTopLeft]) + fDeltaX * Color.red(pixelsOrig[clrTopRight])
                fTopGreen = (1 - fDeltaX) * Color.green(pixelsOrig[clrTopLeft]) + fDeltaX * Color.green(pixelsOrig[clrTopRight])
                fTopBlue = (1 - fDeltaX) * Color.blue(pixelsOrig[clrTopLeft]) + fDeltaX * Color.blue(pixelsOrig[clrTopRight])

                // linearly interpolate horizontally between bottom neighbours
                fBottomRed = (1 - fDeltaX) * Color.red(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.red(pixelsOrig[clrBottomRight])
                fBottomGreen = (1 - fDeltaX) * Color.green(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.green(pixelsOrig[clrBottomRight])
                fBottomBlue = (1 - fDeltaX) * Color.blue(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.blue(pixelsOrig[clrBottomRight])

                // linearly interpolate vertically between top and bottom interpolated results
                iRed = ((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed).roundToInt()
                iGreen = ((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen).roundToInt()
                iBlue = ((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue).roundToInt()

                pixelsNew[i * new.width + j] = Color.rgb(iRed, iGreen, iBlue)
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    private fun scale(orig: Bitmap, scaleFactor: Double): Bitmap {
        val new = createBitmap((orig.width * scaleFactor).toInt(), (orig.height * scaleFactor).toInt(), Bitmap.Config.ARGB_8888)
        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)

        var iFloorX: Int
        var iCeilingX: Int
        var iFloorY: Int
        var iCeilingY: Int
        var fTrueX: Double
        var fTrueY: Double
        var fDeltaX: Double
        var fDeltaY: Double
        var clrTopLeft: Int
        var clrTopRight: Int
        var clrBottomLeft: Int
        var clrBottomRight: Int
        var fTopRed: Double
        var fTopGreen: Double
        var fTopBlue: Double
        var fBottomRed: Double
        var fBottomGreen: Double
        var fBottomBlue: Double
        var iRed: Int
        var iGreen: Int
        var iBlue: Int

        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                fTrueX = j / scaleFactor
                fTrueY = i / scaleFactor

                iFloorX = floor(fTrueX).toInt()
                iFloorY = floor(fTrueY).toInt()
                iCeilingX = ceil(fTrueX).toInt()
                iCeilingY = ceil(fTrueY).toInt()
                if (iFloorX < 0 || iCeilingX >= orig.width || iFloorY < 0 || iCeilingY >= orig.height) continue

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * orig.width + iFloorX
                clrTopRight = iFloorY * orig.width + iCeilingX
                clrBottomLeft = iCeilingY * orig.width + iFloorX
                clrBottomRight = iCeilingY * orig.width + iCeilingX

                // linearly interpolate horizontally between top neighbours
                fTopRed = (1 - fDeltaX) * Color.red(pixelsOrig[clrTopLeft]) + fDeltaX * Color.red(pixelsOrig[clrTopRight])
                fTopGreen = (1 - fDeltaX) * Color.green(pixelsOrig[clrTopLeft]) + fDeltaX * Color.green(pixelsOrig[clrTopRight])
                fTopBlue = (1 - fDeltaX) * Color.blue(pixelsOrig[clrTopLeft]) + fDeltaX * Color.blue(pixelsOrig[clrTopRight])

                // linearly interpolate horizontally between bottom neighbours
                fBottomRed = (1 - fDeltaX) * Color.red(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.red(pixelsOrig[clrBottomRight])
                fBottomGreen = (1 - fDeltaX) * Color.green(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.green(pixelsOrig[clrBottomRight])
                fBottomBlue = (1 - fDeltaX) * Color.blue(pixelsOrig[clrBottomLeft]) + fDeltaX * Color.blue(pixelsOrig[clrBottomRight])

                // linearly interpolate vertically between top and bottom interpolated results
                iRed = ((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed).roundToInt()
                iGreen = ((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen).roundToInt()
                iBlue = ((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue).roundToInt()

                pixelsNew[i * new.width + j] = Color.rgb(iRed, iGreen, iBlue)
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    /*am 5 means amount is 5%*/
    private fun unsharpMasking(orig: Bitmap, am: Int, rad: Int, thres: Int): Bitmap {
        // should I really crop it?
        val new = createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)

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

        for (i in 0 until new.height) {
            for (j in 0 until new.width) {
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(new.width - 1, max(0, j + k))
                    convolvedHorizontallyRed += Color.red(orig.getPixel(kAdjusted, i)) * gaussianDistribution[k]
                    convolvedHorizontallyGreen += Color.green(orig.getPixel(kAdjusted, i)) * gaussianDistribution[k]
                    convolvedHorizontallyBlue += Color.blue(orig.getPixel(kAdjusted, i)) * gaussianDistribution[k]
                }
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(new.height - 1, max(0, i + k))
                    convolvedVerticallyRed += Color.red(orig.getPixel(j, kAdjusted)) * gaussianDistribution[k]
                    convolvedVerticallyGreen += Color.green(orig.getPixel(j, kAdjusted)) * gaussianDistribution[k]
                    convolvedVerticallyBlue += Color.blue(orig.getPixel(j, kAdjusted)) * gaussianDistribution[k]
                }
                red = Color.red(orig.getPixel(j, i))
                green = Color.green(orig.getPixel(j, i))
                blue = Color.blue(orig.getPixel(j, i))
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
                new.setPixel(j, i, Color.rgb(red, green, blue))

                convolvedHorizontallyRed = 0.0
                convolvedHorizontallyGreen = 0.0
                convolvedHorizontallyBlue = 0.0
                convolvedVerticallyRed = 0.0
                convolvedVerticallyGreen = 0.0
                convolvedVerticallyBlue = 0.0
            }
        }

        return new
    }

    private fun apply(orig: ImageView, new: ImageView/*, value: Double*/) {
        /*unsharpMasking((orig.drawable as BitmapDrawable).bitmap, 800, 1, 10).apply {
            new.setImageBitmap(this)
        }*/
        /*coloredFilter((orig.drawable as BitmapDrawable).bitmap, 0xffff00).apply {
            new.setImageBitmap(this)
        }*/
        /*blackAndWhiteFilter((orig.drawable as BitmapDrawable).bitmap).apply {
            new.setImageBitmap(this)
        }*/
        /*rotateClockwiseByDegrees((orig.drawable as BitmapDrawable).bitmap, 45).apply {
            new.setImageBitmap(this)
        }*/
        scale((orig.drawable as BitmapDrawable).bitmap, 0.05).apply {
            new.setImageBitmap(this)
        }
        /*rotate90DegreesClockwise((orig.drawable as BitmapDrawable).bitmap). apply {
            new.setImageBitmap(this)
        }*/
    }
}
