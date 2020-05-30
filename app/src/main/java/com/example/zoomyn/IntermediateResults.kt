package com.example.zoomyn

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
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
            println("BEFORE SAVING")
            saveImageToStream(bitmap, FileOutputStream(file))
            println("SAVED")
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
            println("ADDED CONTENT VALUES")
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun save(uri: Uri, context: Context) {
        println("STARTED EDITING")
        val tStart = System.currentTimeMillis()
        var code: Double
        val resultList = mutableListOf<Bitmap>()
        resultList.add(
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        )
        while (functionCalls.count() > 0) {
            code = functionCalls[0]
            // Pops out the code. Pop all function argument values in every case of when!
            functionCalls.removeAt(0)

            when (code) {
                1.0 -> resultList.add(blackAndWhiteFilter(resultList[0]))
                2.0 -> resultList.add(grayScaleFilter(resultList[0]))
                3.0 -> resultList.add(sepiaFilter(resultList[0]))
                4.0 -> resultList.add(negativeFilter(resultList[0]))
                5.0 -> {
                    val color = functionCalls[0].toInt()
                    functionCalls.removeAt(0)
                    resultList.add(coloredFilter(resultList[0], color))
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
                7.0 -> resultList.add(callRotate90DegreesClockwise(resultList[0]))
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
                    resultList.add(callUnsharpMasking(resultList[0], amount, radius, threshold))
                }
            }

            resultList.removeAt(0)
        }

        val tEnd = System.currentTimeMillis()
        println("EDITED, took ${(tEnd - tStart).toDouble() / 1000}")
        saveImage(resultList[0], context, "Zoomyn")
        println("ALL DONE")
        bitmapsList.removeAll(bitmapsList)
    }

    fun undo(bitmapToUndo: Bitmap): Bitmap {
        if (bitmapsList.count() > 1) {
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
            return bitmapsList.last()
        }

        return bitmapToUndo
    }

    private fun _rotateClockwiseByDegrees(iCentreX: Int, iCentreY: Int, a: Double, nw: Int, ow: Int, pixelsOrig: IntArray, pixelsNew: IntArray, oh: Int, iMin: Int, iMax: Int) {
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

        for (i in iMin..iMax) {
            for (j in 0 until nw) {
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
                if (iFloorX < 0 || iCeilingX >= ow || iFloorY < 0 || iCeilingY >= oh) {
                    pixelsNew[i * nw + j] = Color.WHITE
                    continue
                }

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * ow + iFloorX
                clrTopRight = iFloorY * ow + iCeilingX
                clrBottomLeft = iCeilingY * ow + iFloorX
                clrBottomRight = iCeilingY * ow + iCeilingX

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

                pixelsNew[i * nw + j] = Color.rgb(iRed, iGreen, iBlue)
            }
        }
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

        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)
        /*for (i in 0 until new.height) {
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
        }*/
        runBlocking {
            coroutineScope {
                val n = 1275 // amount of coroutines to launch
                IntRange(0, n - 1).map {
                    async(Dispatchers.Default) {
                        _rotateClockwiseByDegrees(
                            iCentreX,
                            iCentreY,
                            a,
                            new.width,
                            orig.width,
                            pixelsOrig,
                            pixelsNew,
                            orig.height,
                            it * new.height / n,
                            if (it != n - 1) { (it + 1) * new.height / n - 1 } else { new.height - 1 }
                        )
                    }
                }.awaitAll()
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    private fun _getPixelsBilinearlyScaled(iMin: Int, iMax: Int, nw: Int, scaleFactor: Double, ow: Int, oh: Int, orig: IntArray, pixelsNew: IntArray) {
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

        for (i in iMin..iMax) {
            for (j in 0 until nw) {
                fTrueX = j / scaleFactor
                fTrueY = i / scaleFactor

                iFloorX = floor(fTrueX).toInt()
                iFloorY = floor(fTrueY).toInt()
                iCeilingX = ceil(fTrueX).toInt()
                iCeilingY = ceil(fTrueY).toInt()
                if (iFloorX < 0 || iCeilingX >= ow || iFloorY < 0 || iCeilingY >= oh) continue

                fDeltaX = fTrueX - iFloorX
                fDeltaY = fTrueY - iFloorY

                // indices in pixelsOrig:
                clrTopLeft = iFloorY * ow + iFloorX
                clrTopRight = iFloorY * ow + iCeilingX
                clrBottomLeft = iCeilingY * ow + iFloorX
                clrBottomRight = iCeilingY * ow + iCeilingX

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
    }

    private fun getPixelsBilinearlyScaled(orig: IntArray, scaleFactor: Double, origWidth: Int, origHeight: Int): IntArray {
        val nw = (origWidth * scaleFactor).roundToInt()
        val nh = (origHeight * scaleFactor).roundToInt()
        val pixelsNew = IntArray(nw * nh)

        runBlocking {
            coroutineScope {
                val n = 1275 // amount of coroutines to launch
                IntRange(0, n - 1).map {
                    async(Dispatchers.Default) {
                        _getPixelsBilinearlyScaled(
                            it * nh / n,
                            if (it != n - 1) { (it + 1) * nh / n - 1 } else { nh - 1 },
                            nw,
                            scaleFactor,
                            origWidth,
                            origHeight,
                            orig,
                            pixelsNew
                        )
                    }
                }.awaitAll()
            }
        }

        return pixelsNew
    }

    private fun interpolateBetweenPixels(
        iMin: Int,
        iMax: Int,
        nw: Int,
        result: IntArray,
        weightOfBigger: Double,
        pixelsFromSmaller: IntArray,
        pixelsFromBigger: IntArray,
        widthDiffFromFromBigger: Int,
        widthDiffFromFromSmaller: Int
    ) {
        for (i in iMin..iMax) {
            for (j in 0 until nw) {
                result[i * nw + j] = Color.rgb(
                    (Color.red(pixelsFromSmaller[i * (nw + widthDiffFromFromSmaller) + j]) * (1 - weightOfBigger)
                            + Color.red(pixelsFromBigger[i * (nw + widthDiffFromFromBigger) + j]) * weightOfBigger).roundToInt(),

                    (Color.green(pixelsFromSmaller[i * (nw + widthDiffFromFromSmaller) + j]) * (1 - weightOfBigger)
                            + Color.green(pixelsFromBigger[i * (nw + widthDiffFromFromBigger) + j]) * weightOfBigger).roundToInt(),

                    (Color.blue(pixelsFromSmaller[i * (nw + widthDiffFromFromSmaller) + j]) * (1 - weightOfBigger)
                            + Color.blue(pixelsFromBigger[i * (nw + widthDiffFromFromBigger) + j]) * weightOfBigger).roundToInt()
                )
            }
        }
    }

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
                    runBlocking {
                        coroutineScope {
                            val n = 1275 // amount of coroutines to launch
                            IntRange(0, n - 1).map {
                                async(Dispatchers.Default) {
                                    interpolateBetweenPixels(
                                        it * new.height / n,
                                        if (it != n - 1) { (it + 1) * new.height / n - 1 } else { new.height - 1 },
                                        new.width,
                                        pixelsResulting,
                                        weightOfBigger,
                                        pixelsFromSmaller,
                                        pixelsFromBigger,
                                        widthDiffFromFromBigger,
                                        widthDiffFromFromSmaller
                                    )
                                }
                            }.awaitAll()
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

    fun negativeFilter(orig: Bitmap): Bitmap {
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

    private fun filter(rule: String, iMin: Int, iMax: Int, pixels: IntArray) {
        when (rule) {
            "s" -> /*{println("$iMin $iMax");*/ for (i in iMin..iMax) {
                /*outputRed = (inputRed * .393) + (inputGreen *.769) + (inputBlue * .189)
                outputGreen = (inputRed * .349) + (inputGreen *.686) + (inputBlue * .168)
                outputBlue = (inputRed * .272) + (inputGreen *.534) + (inputBlue * .131)

                if greater than 255, round to 255*/
                pixels[i] = Color.rgb(
                    min(255, (0.393 * Color.red(pixels[i]) + 0.769 * Color.green(pixels[i]) + 0.189 * Color.blue(pixels[i])).roundToInt()),
                    min(255, (0.349 * Color.red(pixels[i]) + 0.686 * Color.green(pixels[i]) + 0.168 * Color.blue(pixels[i])).roundToInt()),
                    min(255, (0.272 * Color.red(pixels[i]) + 0.534 * Color.green(pixels[i]) + 0.131 * Color.blue(pixels[i])).roundToInt())
                )
            }
        }
    }

    fun sepiaFilter(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)

        /*runBlocking {
            coroutineScope {
                val n = 2550 // amount of coroutines to launch
                IntRange(0, n - 1).map {
                    async(Dispatchers.Default) {
                        println(it * pixels.size / n)
//                        println(if (it != n - 1) {(it + 1) * pixels.size / n - 1} else {pixels.size - 1})
//                        filter("s", it * pixels.size / n, if (it != n - 1) {(it + 1) * pixels.size / n - 1} else {pixels.size - 1}, pixels)
                    }
                }.awaitAll()
            }
        }*/
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

    fun grayScaleFilter(orig: Bitmap): Bitmap {
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

    fun blackAndWhiteFilter(orig: Bitmap): Bitmap {
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

    fun coloredFilter(orig: Bitmap, col: Int): Bitmap {
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(orig.width * orig.height)

        orig.getPixels(pixels, 0, orig.width, 0, 0, orig.width, orig.height)

        for (i in pixels.indices) {
            pixels[i] = col and pixels[i]
        }

        new.setPixels(pixels, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    private fun rotate90DegreesClockwise(orig: IntArray, new: IntArray, totalPixels: Int, ow: Int, nw: Int, iMin: Int, iMax: Int) {
        for (i in iMin..iMax) {
            for (j in 0 until nw) {
                new[i * nw + j] = orig[totalPixels - (j + 1) * ow + i]
            }
        }
    }

    fun callRotate90DegreesClockwise(orig: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(orig.height, orig.width, Bitmap.Config.ARGB_8888)

        val pixelsOrig = IntArray(orig.width * orig.height)
        val pixelsNew = IntArray(new.width * new.height)
        val pixelsCount = orig.width * orig.height
        orig.getPixels(pixelsOrig, 0, orig.width, 0, 0, orig.width, orig.height)

        runBlocking {
            coroutineScope {
                val n = 1275 // amount of coroutines to launch
                IntRange(0, n - 1).map {
                    async(Dispatchers.Default) {
                        rotate90DegreesClockwise(
                            pixelsOrig,
                            pixelsNew,
                            pixelsCount,
                            orig.width,
                            new.width,
                            it * new.height / n,
                            if (it != n - 1) { (it + 1) * new.height / n - 1 } else { new.height - 1 }
                        )
                    }
                }.awaitAll()
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)

        return new
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
        maxPossibleY: Int,
        radius: Int
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
                    kAdjusted = min(bitmapWidth - 1, max(0, j + k - radius))
                    convolvedHorizontallyRed += Color.red(pixelsOrig[i * bitmapWidth + kAdjusted]) * gaussianDistribution[k]
                    convolvedHorizontallyGreen += Color.green(pixelsOrig[i * bitmapWidth + kAdjusted]) * gaussianDistribution[k]
                    convolvedHorizontallyBlue += Color.blue(pixelsOrig[i * bitmapWidth + kAdjusted]) * gaussianDistribution[k]
                }
                for (k in gaussianDistribution.indices) {
                    kAdjusted = min(maxPossibleY, max(0, i + k - radius))
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
        val new = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
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
                val n = 1275 // amount of coroutines to launch
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
                            new.height - 1,
                            radius
                        )
                    }
                }.awaitAll()
            }
        }

        new.setPixels(pixelsNew, 0, new.width, 0, 0, new.width, new.height)
        return new
    }

    /*runBlocking {
        coroutineScope {
            val n = 1275 // amount of coroutines to launch
            IntRange(0, n - 1).map {
                async(Dispatchers.Default) {
                    for (i in (it * new.height / n)..(if (it != n - 1) { (it + 1) * new.height / n - 1 } else { new.height - 1 })) {
                        for (j in 0 until new.width) {

                        }
                    }
                }
            }.awaitAll()
        }
    }*/
}