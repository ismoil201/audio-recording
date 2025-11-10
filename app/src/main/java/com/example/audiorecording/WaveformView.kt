package com.example.audiorecording

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView@JvmOverloads constructor(context: Context, attrs : AttributeSet? = null, defStyleAttr :Int = 0
): View(context, attrs, defStyleAttr) {


    private val rectWidth = 10f
    private var tick: Int = 0

    private val ampList = mutableListOf<Float>()
    private val rectList = mutableListOf<RectF>()

    val redPaint = Paint().apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (rectF in rectList){

            canvas?.drawRect(rectF, redPaint)

        }

    }

    fun addAmplitude(maxAmplitude : Float) {
      val amplitude =  ( maxAmplitude/ Short.MAX_VALUE) * this.height * 0.8f



        ampList.add(amplitude)
        rectList.clear()
        val maxRectF = (this.width/ rectWidth).toInt()

        val amps = ampList.takeLast(maxRectF)

        for((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top=(this.height /2 )-amp /2-2f
            rectF.bottom =  rectF.top +amp+2
            rectF.left = i * rectWidth
            rectF.right = rectF.left  + rectWidth-5f

            rectList.add(rectF)

        }
        invalidate()
    }


    fun replyAmplitude(duration : Int){
        rectList.clear()
        val maxRect = (this.width/ rectWidth).toInt()

        val amps = ampList.take(tick).takeLast(maxRect)

        for((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top=(this.height /2 )-amp /2-2f
            rectF.bottom =  rectF.top +amp+2
            rectF.left = i * rectWidth
            rectF.right = rectF.left  + rectWidth -5f

            rectList.add(rectF)

        }
        tick++
        invalidate()
    }

    fun clearData(){
        ampList.clear()
    }

    fun clearWave(){
        rectList.clear()
        tick = 0
        invalidate()
    }
}