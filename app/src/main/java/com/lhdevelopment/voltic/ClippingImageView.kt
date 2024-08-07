package com.lhdevelopment.voltic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView

class ClippingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var revealProgress: Float = 0f
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        Log.d("ClippingImageView", "onDraw called with revealProgress = $revealProgress")
        path.reset()
        path.addRect(0f, 0f, width * revealProgress, height.toFloat(), Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }

    fun setRevealProgress(progress: Float) {
        Log.d("ClippingImageView", "setRevealProgress called with progress = $progress")
        revealProgress = progress
        invalidate()
    }

    fun getRevealProgress(): Float {
        return revealProgress
    }
}


