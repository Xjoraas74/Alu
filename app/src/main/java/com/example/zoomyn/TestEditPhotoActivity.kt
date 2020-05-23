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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private fun apply() {
        CoroutineScope(Dispatchers.Default).launch {
            // function here will be executed in parallel with actions following this coroutine
        }
        // in parallel with this
    }
}
