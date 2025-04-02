package com.johncolani.solomoncompass

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        // Ensure the background is transparent
        setBackgroundColor(Color.TRANSPARENT)
    }

    var direction: Float = 0f
        set(value) {
            field = value % 360f
            android.util.Log.d("CompassView", "Direction set to: $field")
            invalidate()
        }

    // Border properties
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = dpToPx(4f) // 4dp
    }

    // Needle image properties
    private var arrowBitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Fallback needle paint (white arrow)
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // Center dot paint
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
    }

    // Cardinal directions paint
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        // Commenting out custom font since R.font.oi may not exist
        // typeface = ResourcesCompat.getFont(context, R.font.oi)
    }

    fun setBorderColor(color: Int) {
        borderPaint.color = color
        invalidate()
    }

    fun setBorderWidth(width: Float) {
        borderPaint.strokeWidth = dpToPx(width)
        invalidate()
    }

    fun setArrowImage(resId: Int) {
        arrowBitmap = BitmapFactory.decodeResource(resources, resId)
        if (arrowBitmap == null) {
            android.util.Log.e("CompassView", "Failed to load arrow image with resId: $resId")
        } else {
            android.util.Log.d("CompassView", "Arrow image loaded: ${arrowBitmap!!.width} x ${arrowBitmap!!.height}")
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (centerX.coerceAtMost(centerY)) * 0.8f

        // Draw white border (no background circle for transparency)
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Draw cardinal directions
        canvas.drawText("N", centerX, centerY - radius + 50, textPaint)
        canvas.drawText("S", centerX, centerY + radius - 20, textPaint)
        canvas.drawText("E", centerX + radius - 40, centerY + 10, textPaint)
        canvas.drawText("W", centerX - radius + 40, centerY + 10, textPaint)

        // Draw rotating arrow
        canvas.save()
        canvas.rotate(-direction, centerX, centerY)

        if (arrowBitmap != null) {
            // Calculate scaled bitmap size, preserving aspect ratio
            val bitmapWidth = arrowBitmap!!.width
            val bitmapHeight = arrowBitmap!!.height
            val aspectRatio = bitmapWidth.toFloat() / bitmapHeight

            // Set the target height to 80% of the radius (adjusted for this detailed image)
            val targetHeight = radius * 0.8f
            val targetWidth = targetHeight * aspectRatio

            val scaledBitmap = Bitmap.createScaledBitmap(
                arrowBitmap!!,
                targetWidth.toInt(),
                targetHeight.toInt(),
                true
            )
            // Adjust position to align the center of the arrow with the center dot
            canvas.drawBitmap(
                scaledBitmap,
                centerX - targetWidth / 2,
                centerY - targetHeight / 2,
                bitmapPaint
            )
            android.util.Log.d("CompassView", "Drawing arrow: $targetWidth x $targetHeight at direction: $direction")
        } else {
            // Fallback: Draw a simple white arrow
            val needleLength = radius * 0.8f
            val needleWidth = 20f
            val path = Path().apply {
                moveTo(centerX, centerY - needleLength) // Tip
                lineTo(centerX - needleWidth, centerY) // Bottom left
                lineTo(centerX + needleWidth, centerY) // Bottom right
                close()
            }
            canvas.drawPath(path, needlePaint)
        }

        canvas.restore()

        // Draw center dot
        canvas.drawCircle(centerX, centerY, 10f, centerPaint)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}