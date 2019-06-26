package it.arkadiuss.progresstracer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ProgressTracer(context: Context, attr: AttributeSet) : View(context,attr){

    private val TAG = this.javaClass.name
    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    var text: String = ""
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    private var thickness = 20f
    private var startPoint = 110f
    private var degLen = 320f
    private var radius = 500
    private var remainingColor = ContextCompat.getColor(context, android.R.color.black)
    private var completedColor = ContextCompat.getColor(context, android.R.color.black)

    private lateinit var oval: RectF
    private lateinit var smallOval: RectF
    private lateinit var roundOval: RectF
    private val remainingPath: Path = Path()
    private val completedPath: Path = Path()

    init {
        context.theme.obtainStyledAttributes(
                attr, R.styleable.ProgressTracer, 0, 0
        ).apply {
            try {
                progress = getFloat(R.styleable.ProgressTracer_progress, 0f)
                text = getString(R.styleable.ProgressTracer_text) ?: ""
                thickness = getFloat(R.styleable.ProgressTracer_arc_thickness, 20f)
                radius = getInt(R.styleable.ProgressTracer_radius, 500)
                startPoint = getFloat(R.styleable.ProgressTracer_start_deg, 110f)
                degLen = getFloat(R.styleable.ProgressTracer_arc_len, 320f)
                completedColor = getColor(R.styleable.ProgressTracer_completed_color, completedColor)
                remainingColor = getColor(R.styleable.ProgressTracer_remaining_color, remainingColor)
            }finally {
                recycle()
            }
        }
    }

    private val completedPartPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = completedColor
    }

    private val remainingPartPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = remainingColor
    }

    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw = paddingLeft + paddingRight + 2*radius
        var w: Int = View.resolveSize(minw, widthMeasureSpec)

        val minh = paddingLeft + paddingRight + 2*radius
        var h = View.resolveSize(minh, heightMeasureSpec)
        w = min(w,h)
        h = min(w,h)
        setMeasuredDimension(w,h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        oval = RectF(0f,0f, width*1f, height*1f)
        smallOval = RectF(thickness,thickness, (width-thickness), (height-thickness))
        roundOval = RectF(0f,0f, thickness, thickness)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            val divPoint = degLen*progress/100f
            remainingPath.reset()
            roundOval.setAsOvalForRounding(startPoint.toDouble())
            remainingPath.apply {
                arcTo(oval,startPoint, divPoint)
                arcTo(smallOval, startPoint + divPoint, -divPoint)
                arcTo(roundOval, startPoint, -180f)
                close()
            }
            drawPath(remainingPath,remainingPartPaint)

            completedPath.reset()
            roundOval.setAsOvalForRounding((startPoint + degLen).toDouble())
            completedPath.apply {
                arcTo(oval,startPoint + divPoint, degLen - divPoint)
                arcTo(roundOval, startPoint + degLen, 180f)
                arcTo(smallOval, startPoint + degLen, -(degLen - divPoint))
                close()
            }
            drawPath(completedPath,completedPartPaint)

            drawText(text,width/2f, height/2f, textPaint)
        }
    }

    private fun RectF.setAsOvalForRounding(angle: Double){
        val roundRadius  = thickness/2f
        val radAng = Math.toRadians(angle)
        val x: Float = width/2 + ((width/2 - roundRadius) * Math.cos(radAng)).toFloat()
        val y: Float = height/2 + ((height/2 - roundRadius) * Math.sin(radAng)).toFloat()
        this.set(x - roundRadius,
                y - roundRadius,
                x + roundRadius,
                y+roundRadius)
    }
}