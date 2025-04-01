package com.johncolani.solomoncompass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View


class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var direction: Float = 0f
        set(value) {
            field = value % 360f
            invalidate()
        }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val needleAngle = Math.toRadians(direction.toDouble())
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (centerX.coerceAtMost(centerY)) * 0.8f

        // Draw compass background
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw cardinal directions
        canvas.drawText("N", centerX, centerY - radius + 50, textPaint)
        canvas.drawText("S", centerX, centerY + radius - 20, textPaint)
        canvas.drawText("E", centerX + radius - 40, centerY + 10, textPaint)
        canvas.drawText("W", centerX - radius + 40, centerY + 10, textPaint)

        // Draw rotating needle
        canvas.save()
        canvas.rotate(-direction, centerX, centerY)

        val needlePath = Path().apply {
            moveTo(centerX, centerY - radius * 0.7f)  // Needle tip
            lineTo(centerX - 20f, centerY)
            lineTo(centerX + 20f, centerY)
            close()
        }
        canvas.drawPath(needlePath, needlePaint)
        canvas.restore()

        // Draw center dot
        canvas.drawCircle(centerX, centerY, 10f, needlePaint)
    }
}