package com.example.zoomyn

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.math.*


class IntermediateResults : Application() {
    // for undo
    val bitmapsList = mutableListOf<Bitmap>()

    // to remember what functions must be called on original image
    val functionCalls = mutableListOf<Double>()

    /// @param folderName can be your app's name
    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun save(uri: Uri, context: Context) {
        var code: Double
        val resultList = mutableListOf<Bitmap>()
        resultList.add(
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        )
        println("${functionCalls.count()}")
        println("${functionCalls[0]} ${functionCalls[1]}")
        while (functionCalls.count() > 0) {
            code = functionCalls[0]
            // Pops out the code. Pop all function argument values in every case of when!
            functionCalls.removeAt(0)
            println("$functionCalls")

            when (code) {
                1.0 -> resultList.add((Activity() as EditPhotoActivity).blackAndWhiteFilter(resultList[0]))
                2.0 -> resultList.add((Activity() as EditPhotoActivity).grayScaleFilter(resultList[0]))
                3.0 -> resultList.add((Activity() as EditPhotoActivity).sepiaFilter(resultList[0]))
                4.0 -> resultList.add((Activity() as EditPhotoActivity).negativeFilter(resultList[0]))
                5.0 -> {
                    val color = functionCalls[0].toInt()
                    functionCalls.removeAt(0)
                    resultList.add((Activity() as EditPhotoActivity).coloredFilter(resultList[0], color))
                }
                6.0 -> {
                    val factor = functionCalls[0]
                    functionCalls.removeAt(0)
                    val mipmaps = mutableListOf<FunScaleActivity.Mipmap>()
                    if (factor < 1.0) {
                        createMipmaps(mipmaps, resultList[0])
                    }
                    resultList.add(scale(resultList[0], factor, mipmaps))
                }
                7.0 -> resultList.add((Activity() as FunTurn90DegreesActivity).rotate90DegreesClockwise(resultList[0]))
                8.0 -> {
                    val angle = functionCalls[0].toInt()
                    functionCalls.removeAt(0)
                    resultList.add(rotateClockwiseByDegrees(resultList[0], angle))
                }
                9.0 -> {
                    val amount = functionCalls[0].toInt()
                    val radius = functionCalls[1].toInt()
                    val threshold = functionCalls[2].toInt()
                    functionCalls.removeAt(0)
                    functionCalls.removeAt(0)
                    functionCalls.removeAt(0)
                    resultList.add((Activity() as FunMaskingActivity).callUnsharpMasking(resultList[0], amount, radius, threshold))
                }
            }

            resultList.removeAt(0)
        }

        saveImage(resultList[0], context, "Zoomyn")
        println("IT'S ALL DONE")
        bitmapsList.removeAll(bitmapsList)
    }

    fun undo() {
        var k = 0
        var kPrev = k

        while (k < functionCalls.count()) {
            kPrev = k
            when (functionCalls[k]) {
                1.0, 2.0, 3.0, 4.0, 7.0 -> k++
                5.0, 6.0, 8.0 -> k += 2
                9.0 -> k += 4
            }
        }
        when (functionCalls[kPrev]) {
            1.0, 2.0, 3.0, 4.0, 7.0 -> functionCalls.removeAt(functionCalls.lastIndex)
            5.0, 6.0, 8.0 -> {
                functionCalls.removeAt(functionCalls.lastIndex)
                functionCalls.removeAt(functionCalls.lastIndex)
            }
            9.0 -> {
                functionCalls.removeAt(functionCalls.lastIndex)
                functionCalls.removeAt(functionCalls.lastIndex)
                functionCalls.removeAt(functionCalls.lastIndex)
                functionCalls.removeAt(functionCalls.lastIndex)
            }
        }

        bitmapsList.removeAt(bitmapsList.lastIndex)
    }

    fun rotateClockwiseByDegrees(orig: Bitmap, aDeg: Int): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
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
                if (iFloorX < 0 || iCeilingX >= orig.width || iFloorY < 0 || iCeilingY >= orig.height) {
                    pixelsNew[i * new.width + j] = Color.WHITE
                    continue
                }

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

                iFloorX = kotlin.math.floor(fTrueX).toInt()
                iFloorY = kotlin.math.floor(fTrueY).toInt()
                iCeilingX = kotlin.math.ceil(fTrueX).toInt()
                iCeilingY = kotlin.math.ceil(fTrueY).toInt()
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

    //функция масштабирования полученного изображения
    fun scale(orig: Bitmap, scaleFactor: Double, mipmaps: MutableList<FunScaleActivity.Mipmap>): Bitmap {
        if ((orig.width * scaleFactor).roundToInt() < 1 || (orig.height * scaleFactor).roundToInt() < 1) {
            val new = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            new.setPixel(0, 0, mipmaps.last().self[0])
            Toast.makeText(this, "The value is too small; can't resize!", Toast.LENGTH_LONG).show()
            return new
        }

        if (scaleFactor == 1.0) {
            return orig
        }

        val new = Bitmap.createBitmap(
            (orig.width * scaleFactor).roundToInt(),
            (orig.height * scaleFactor).roundToInt(),
            Bitmap.Config.ARGB_8888
        )

        if (scaleFactor < 1.0) {
            var scale = 1.0
            var scalePrev = scale

            for (i in mipmaps.indices) {
                if (scale <= scaleFactor) {
                    // interpolates between i and i - 1 into pixelsResulting[]
                    val pixelsResulting = IntArray(new.width * new.height)
                    val weightOfBigger = (scaleFactor - scale) / (scalePrev - scale)

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

                    // fixes scaling from mipmaps giving wrong amount of pixels in width
                    val widthDiffFromFromBigger = (scaleDown * mipmaps[i - 1].width).roundToInt() - new.width
                    val widthDiffFromFromSmaller = (scaleUp * mipmaps[i].width).roundToInt() - new.width
                    for (i in 0 until new.height) {
                        for (j in 0 until new.width) {
                            pixelsResulting[i * new.width + j] = Color.rgb(
                                (Color.red(pixelsFromSmaller[i * (new.width + widthDiffFromFromSmaller) + j]) * (1 - weightOfBigger)
                                        + Color.red(pixelsFromBigger[i * (new.width + widthDiffFromFromBigger) + j]) * weightOfBigger).roundToInt(),

                                (Color.green(pixelsFromSmaller[i * (new.width + widthDiffFromFromSmaller) + j]) * (1 - weightOfBigger)
                                        + Color.green(pixelsFromBigger[i * (new.width + widthDiffFromFromBigger) + j]) * weightOfBigger).roundToInt(),

                                (Color.blue(pixelsFromSmaller[i * (new.width + widthDiffFromFromSmaller) + j]) * (1 - weightOfBigger)
                                        + Color.blue(pixelsFromBigger[i * (new.width + widthDiffFromFromBigger) + j]) * weightOfBigger).roundToInt()
                            )
                        }
                    }

                    new.setPixels(pixelsResulting, 0, new.width, 0, 0, new.width, new.height)
                    return new
                }

                scalePrev = scale
                scale *= .5
            }
        }

        val pixelsOrig = IntArray(orig.width * orig.height)
        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        new.setPixels(
            getPixelsBilinearlyScaled(pixelsOrig, scaleFactor, orig.width, orig.height),
            0,
            new.width,
            0,
            0,
            new.width,
            new.height
        )
        return new
    }

    fun createMipmaps(m: MutableList<FunScaleActivity.Mipmap>, orig: Bitmap) {
        FunScaleActivity.Mipmap().apply {
            this.create(orig.width, orig.height)
            orig.getPixels(this.self, 0, orig.width, 0, 0, orig.width, orig.height)
            m.add(this)
        }

        // used    do { } while ()    before
        while (m.last().width != 1 || m.last().height != 1) {
            FunScaleActivity.Mipmap().apply {
                this.create((m.last().width * .5).roundToInt(), (m.last().height * .5).roundToInt())
                this.self = getPixelsBilinearlyScaled(m.last().self, .5, m.last().width, m.last().height)
                m.add(this)
            }
        }
    }
}