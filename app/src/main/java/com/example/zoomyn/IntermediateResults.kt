package com.example.zoomyn

import android.app.Application
import android.graphics.Bitmap

class IntermediateResults : Application() {
    val list = mutableListOf<Bitmap>()
    val functionCalls = mutableListOf<Int>()

    fun save() {

    }
}