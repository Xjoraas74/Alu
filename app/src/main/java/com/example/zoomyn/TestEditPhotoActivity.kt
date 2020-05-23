package com.example.zoomyn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_test_edit_photo.*
import kotlin.math.*

class TestEditPhotoActivity : AppCompatActivity() {

    // for scaling
    class Mipmap {
        lateinit var self: IntArray
        var width = 0
        var height = 0

        fun create(width: Int, height: Int) {
            this.height = height
            this.width = width
            this.self = IntArray(height * width)
        }
    }

    private val mipmaps = mutableListOf<Mipmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_edit_photo)

        val value = .02

        tri.setOnClickListener {
            imageViewNew.setImageBitmap(scale((imageViewOrig.drawable as BitmapDrawable).bitmap, value, mipmaps))
        }

        bi.setOnClickListener {
            val obit = createBitmap((imageViewOrig.drawable as BitmapDrawable).bitmap)
            val o = IntArray(obit.width * obit.height)
            obit.getPixels(o, 0, obit.width, 0, 0, obit.width, obit.height)
            val b = createBitmap((obit.width*value).roundToInt(),(obit.height*value).roundToInt(),Bitmap.Config.ARGB_8888)
            b.setPixels(getPixelsBilinearlyScaled(o, value, obit.width, obit.height), 0, b.width, 0, 0, b.width, b.height)
            imageViewNew.setImageBitmap(b)
        }

        // for scaling
        createMipmaps(mipmaps, (imageViewOrig.drawable as BitmapDrawable).bitmap)
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

    private fun createMipmaps(m: MutableList<Mipmap>, orig: Bitmap) {
        Mipmap().apply {
            this.create(orig.width, orig.height)
            orig.getPixels(this.self, 0, orig.width, 0, 0, orig.width, orig.height)
            m.add(this)
        }

        do {
            Mipmap().apply {
                this.create((m.last().width * .5).roundToInt(), (m.last().height * .5).roundToInt())
                this.self = getPixelsBilinearlyScaled(m.last().self, .5, m.last().width, m.last().height)
                m.add(this)
            }
        } while (m.last().width != 1 || m.last().height != 1)
    }
    
    private fun getPixelsBilinearlyScaled(orig: IntArray, scaleFactor: Double, origWidth: Int, origHeight: Int): IntArray {
        val nw = (origWidth * scaleFactor).roundToInt()
        val nh = (origHeight * scaleFactor).roundToInt()
        val pixelsNew = IntArray(nw * nh)

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

        for (i in 0 until nh) {
            for (j in 0 until nw) {
                fTrueX = j / scaleFactor
                fTrueY = i / scaleFactor

                iFloorX = floor(fTrueX).toInt()
                iFloorY = floor(fTrueY).toInt()
                iCeilingX = ceil(fTrueX).toInt()
                iCeilingY = ceil(fTrueY).toInt()
                if (iFloorX < 0 || iCeilingX >= origWidth || iFloorY < 0 || iCeilingY >= origHeight) continue

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * origWidth + iFloorX
                clrTopRight = iFloorY * origWidth + iCeilingX
                clrBottomLeft = iCeilingY * origWidth + iFloorX
                clrBottomRight = iCeilingY * origWidth + iCeilingX

                // linearly interpolate horizontally between top neighbours
                fTopRed = (1 - fDeltaX) * Color.red(orig[clrTopLeft]) + fDeltaX * Color.red(orig[clrTopRight])
                fTopGreen = (1 - fDeltaX) * Color.green(orig[clrTopLeft]) + fDeltaX * Color.green(orig[clrTopRight])
                fTopBlue = (1 - fDeltaX) * Color.blue(orig[clrTopLeft]) + fDeltaX * Color.blue(orig[clrTopRight])

                // linearly interpolate horizontally between bottom neighbours
                fBottomRed = (1 - fDeltaX) * Color.red(orig[clrBottomLeft]) + fDeltaX * Color.red(orig[clrBottomRight])
                fBottomGreen = (1 - fDeltaX) * Color.green(orig[clrBottomLeft]) + fDeltaX * Color.green(orig[clrBottomRight])
                fBottomBlue = (1 - fDeltaX) * Color.blue(orig[clrBottomLeft]) + fDeltaX * Color.blue(orig[clrBottomRight])

                // linearly interpolate vertically between top and bottom interpolated results
                iRed = ((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed).roundToInt()
                iGreen = ((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen).roundToInt()
                iBlue = ((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue).roundToInt()

                pixelsNew[i * nw + j] = Color.rgb(iRed, iGreen, iBlue)
            }
        }

        return pixelsNew
    }

    // allegedly uses trilinear filtering for downsclaing, don't delete it, don't use it
    private fun scale(orig: Bitmap, scaleFactor: Double, mipmaps: MutableList<Mipmap>): Bitmap {
        if ((orig.width * scaleFactor).roundToInt() < 1 || (orig.height * scaleFactor).roundToInt() < 1) {
            val new = createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            new.setPixel(0, 0, mipmaps.last().self[0])
            Toast.makeText(this, "The value is too small; can't resize!", Toast.LENGTH_LONG).show()
            return new
        }

        if (scaleFactor == 1.0) {
            return orig
        }

        val new = createBitmap(
            max((orig.width * scaleFactor).roundToInt(), 1),
            max((orig.height * scaleFactor).roundToInt(), 1),
            Bitmap.Config.ARGB_8888
        )
        /*val new = createBitmap(mipmaps[1].width, mipmaps[1].height, Bitmap.Config.ARGB_8888)
        new.setPixels(mipmaps[1].self, 0, mipmaps[1].width, 0, 0, mipmaps[1].width, mipmaps[1].height)
        return new*/

        if (scaleFactor < 1.0) {
            var scale = 1.0
            var scalePrev = scale
            for (i in mipmaps.indices) {
                if (scale <= scaleFactor) {
                    // interpolate between i and i - 1 into pixelsFromBigger[]
                    val deltaScale = scaleFactor - scale
                    val scaleUp = scaleFactor / scale
                    val scaleDown = scaleFactor / scalePrev
                    val pixelsFromBigger = getPixelsBilinearlyScaled(
                        mipmaps[i - 1].self,
                        scaleDown,
                        mipmaps[i - 1].width,
                        mipmaps[i - 1].height
                    )
                    val pixelsFromSmaller = getPixelsBilinearlyScaled(
                        mipmaps[i].self,
                        scaleUp,
                        mipmaps[i].width,
                        mipmaps[i].height
                    )
                    // fixes upscaling giving too many pixels in width...
                    /*val intermediateBitmapFromSmaller = createBitmap(
                        (scaleFactor / scale * mipmaps[i].width).roundToInt(),
                        (scaleFactor / scale * mipmaps[i].height).roundToInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    intermediateBitmapFromSmaller.setPixels(
                        pixelsFromSmaller,
                        0,
                        intermediateBitmapFromSmaller.width,
                        0,
                        0,
                        intermediateBitmapFromSmaller.width,
                        intermediateBitmapFromSmaller.height
                    )*/
//                    intermediateBitmapFromSmaller.getPixels(pixelsFromSmaller, 0, intermediateBitmapFromSmaller.width, 0, 0, intermediateBitmapFromSmaller.width, intermediateBitmapFromSmaller.height)
                    /*for (i in 0 until min(pixelsFromSmaller.size, pixelsFromBigger.size)) {
                        pixelsFromSmaller[i] = Color.rgb(
                            (Color.red(pixelsFromSmaller[i]) * (1 - deltaScale) + Color.red(pixelsFromBigger[i]) * deltaScale).roundToInt(),
                            (Color.green(pixelsFromSmaller[i]) * (1 - deltaScale) + Color.green(pixelsFromBigger[i]) * deltaScale).roundToInt(),
                            (Color.blue(pixelsFromSmaller[i]) * (1 - deltaScale) + Color.blue(pixelsFromBigger[i]) * deltaScale).roundToInt()
                        )
                    }*/
//                    val diffWidth = (scaleUp * mipmaps[i].width).roundToInt() - (scaleDown * mipmaps[i - 1].width).roundToInt()
                    /*println("${(scaleUp * mipmaps[i].width).roundToInt() - (scaleDown * mipmaps[i - 1].width).roundToInt()} " +
                            "= " +
                            "${(scaleUp * mipmaps[i].width).roundToInt()} " +
                            "${(scaleDown * mipmaps[i - 1].width).roundToInt()} ")*/
                    val widthDiffFromFromBigger = (scaleDown * mipmaps[i - 1].width).roundToInt() - new.width
                    val widthDiffFromFromSmaller = (scaleUp * mipmaps[i].width).roundToInt() - new.width
                    println("diff is b $widthDiffFromFromBigger s $widthDiffFromFromSmaller")
                    val pixelsResulting = IntArray(new.width * new.height)
                    for (i in 0 until new.height) {
                        for (j in 0 until new.width) {
                            pixelsResulting[i * new.width + j] = Color.rgb(
                                (Color.red(pixelsFromSmaller[i * (new.width + widthDiffFromFromSmaller) + j]) * (1 - deltaScale)
                                        + Color.red(pixelsFromBigger[i * (new.width + widthDiffFromFromBigger) + j]) * deltaScale).roundToInt(),

                                (Color.green(pixelsFromSmaller[i * (new.width + widthDiffFromFromSmaller) + j]) * (1 - deltaScale)
                                        + Color.green(pixelsFromBigger[i * (new.width + widthDiffFromFromBigger) + j]) * deltaScale).roundToInt(),

                                (Color.blue(pixelsFromSmaller[i * (new.width + widthDiffFromFromSmaller) + j]) * (1 - deltaScale)
                                        + Color.blue(pixelsFromBigger[i * (new.width + widthDiffFromFromBigger) + j]) * deltaScale).roundToInt()
                            )
                        }
                    }

                    println("GO ${new.width} " +
                            "${(scaleDown * mipmaps[i - 1].width).roundToInt()} " +
                            "${(scaleUp * mipmaps[i].width).roundToInt()} " +

                            "${orig.width}")
                    new.setPixels(pixelsResulting, 0, new.width, 0, 0, new.width, new.height)
                    println("Done!;)")
                    return new
                }

                scalePrev = scale
                scale *= .5
            }

            val new = createBitmap(mipmaps[1].width, mipmaps[1].height, Bitmap.Config.ARGB_8888)
            new.setPixels(mipmaps[1].self, 0, mipmaps[1].width, 0, 0, mipmaps[1].width, mipmaps[1].height)
            return new
        }

        val pixels = IntArray(orig.width * orig.height)
        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)
        new.setPixels(
            getPixelsBilinearlyScaled(pixels, scaleFactor, orig.width, orig.height),
            0,
            new.width,
            0,
            0,
            new.width,
            new.height
        )
        return new
    }

    /*am 5 means amount is 5%*/
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
                // don't make convolution for boundary pixels?
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

    private fun apply(orig: ImageView, new: ImageView, valu: Double) {
        /*CoroutineScope(Dispatchers.Default).launch {
            // function here will be executed in parallel with actions following this coroutine
        }
        unsharpMasking((orig.drawable as BitmapDrawable).bitmap, 80, 40, 15).apply {
            new.setImageBitmap(this)
        }*/
        /*rotateClockwiseByDegrees((orig.drawable as BitmapDrawable).bitmap, 45).apply {
            new.setImageBitmap(this)
        }*/

        scale((orig.drawable as BitmapDrawable).bitmap, valu, mipmaps).apply {
            new.setImageBitmap(this)
        }

        /*val obit = createBitmap((orig.drawable as BitmapDrawable).bitmap)
        val o = IntArray(obit.width * obit.height)
        obit.getPixels(o, 0, obit.width, 0, 0, obit.width, obit.height)
        val b = createBitmap((obit.width*valu).roundToInt(),(obit.height*valu).roundToInt(),Bitmap.Config.ARGB_8888)
        b.setPixels(getPixelsBilinearlyScaled(o, valu, obit.width, obit.height), 0, b.width, 0, 0, b.width, b.height)
        new.setImageBitmap(b)*/

        /*rotate90DegreesClockwise((orig.drawable as BitmapDrawable).bitmap). apply {
            new.setImageBitmap(this)
        }*/
    }
}
