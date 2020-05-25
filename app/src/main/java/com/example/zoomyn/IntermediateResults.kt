package com.example.zoomyn

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap

class IntermediateResults : Application() {
    // for undo
    val list = mutableListOf<Bitmap>()

    // to remember what functions must be called on original image
    val functionCalls = mutableListOf<Double>()

    fun save(original: Bitmap) {
        var code: Double
        val resultList = mutableListOf<Bitmap>()
        resultList.add(original)

        while (functionCalls.count() > 0) {
            code = functionCalls[0]
            // Pops out the code. Pop every function argument value in every case of when!
            functionCalls.drop(1)

            when (code) {
                1.0 -> resultList.add((Activity() as EditPhotoActivity).blackAndWhiteFilter(resultList[0]))
                2.0 -> resultList.add((Activity() as EditPhotoActivity).grayScaleFilter(resultList[0]))
                3.0 -> resultList.add((Activity() as EditPhotoActivity).sepiaFilter(resultList[0]))
                4.0 -> resultList.add((Activity() as EditPhotoActivity).negativeFilter(resultList[0]))
                5.0 -> {
                    val color = functionCalls[0].toInt()
                    functionCalls.drop(1)
                    resultList.add((Activity() as EditPhotoActivity).coloredFilter(resultList[0], color))
                }
                6.0 -> {
                    val factor = functionCalls[0]
                    functionCalls.drop(1)
                    val mipmaps = mutableListOf<FunScaleActivity.Mipmap>()
                    if (factor < 1.0) {
                        (Activity() as FunScaleActivity).createMipmaps(mipmaps, resultList[0])
                    }
                    resultList.add((Activity() as FunScaleActivity).scale(resultList[0], factor, mipmaps))
                }
                7.0 -> resultList.add((Activity() as FunTurn90DegreesActivity).rotate90DegreesClockwise(resultList[0]))
                8.0 -> {
                    val angle = functionCalls[0].toInt()
                    functionCalls.drop(1)
                    resultList.add((Activity() as FunTurnArbitraryAngle).rotateClockwiseByDegrees(resultList[0], angle))
                }
                9.0 -> {
                    val amount = functionCalls[0].toInt()
                    val radius = functionCalls[1].toInt()
                    val threshold = functionCalls[2].toInt()
                    functionCalls.drop(3)
                    resultList.add((Activity() as FunMaskingActivity).callUnsharpMasking(resultList[0], amount, radius, threshold))
                }
            }

            resultList.drop(1)
        }
    }
}