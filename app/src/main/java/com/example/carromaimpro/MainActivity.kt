package com.example.carromaimpro

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.*

class MainActivity : Activity() {
    private lateinit var board: PracticeBoard
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(24, 31, 39))
        }
        val title = TextView(this).apply {
            text = "CARROM AIM PRO"
            textSize = 23f
            setTextColor(Color.WHITE)
            setPadding(4, 8, 4, 12)
        }
        status = TextView(this).apply {
            text = "Tap the board to place the striker. Drag to set the aim."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(4, 6, 4, 10)
        }
        board = PracticeBoard(this) { angle, power ->
            status.text = "Aim: ${"%.1f".format(angle)}°   |   Suggested power: ${"%.0f".format(power)}%"
        }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val reset = Button(this).apply {
            text = "Reset"
            setOnClickListener { board.reset(); status.text = "Board reset. Set a new shot." }
        }
        val auto = Button(this).apply {
            text = "Practice Auto Play"
            setOnClickListener {
                board.practiceShot()
                Toast.makeText(this@MainActivity, "Practice shot simulated in this app only", Toast.LENGTH_SHORT).show()
            }
        }
        controls.addView(reset, LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(auto, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(title)
        root.addView(status)
        root.addView(board, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(controls)
        setContentView(root)
    }
}

private class PracticeBoard(context: android.content.Context, val onAim: (Double, Double) -> Unit) : View(context) {
    private val wood = Paint(3)
    private val line = Paint(3).apply { color = Color.rgb(35, 105, 220); strokeWidth = 5f; style = Paint.Style.STROKE }
    private val dashed = Paint(3).apply {
        color = Color.rgb(245, 175, 55); strokeWidth = 4f; style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(12f, 9f), 0f)
    }
    private val textPaint = Paint(3).apply { color = Color.WHITE; textSize = 30f; typeface = Typeface.DEFAULT_BOLD }
    private var striker = PointF(.30f, .70f)
    private var target = PointF(.60f, .42f)
    private var pocket = PointF(.90f, .10f)
    private var dragging = false
    private var practiceProgress = 0f
    private var lastTouch = PointF()

    fun reset() {
        striker = PointF(.30f, .70f); target = PointF(.60f, .42f)
        pocket = PointF(.90f, .10f); practiceProgress = 0f; invalidate()
    }

    fun practiceShot() {
        // Visual-only demonstration; does not control any other game.
        practiceProgress = 1f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pad = 24f
        val board = RectF(pad, pad, width - pad, height - pad)
        wood.color = Color.rgb(190, 126, 68); wood.style = Paint.Style.FILL
        canvas.drawRoundRect(board, 28f, 28f, wood)
        val inner = RectF(board.left + 30, board.top + 30, board.right - 30, board.bottom - 30)
        wood.color = Color.rgb(239, 207, 151)
        canvas.drawRoundRect(inner, 12f, 12f, wood)
        val w = inner.width(); val h = inner.height()
        fun pt(p: PointF) = PointF(inner.left + p.x * w, inner.top + p.y * h)
        val s = pt(striker); val t = pt(target); val p = pt(pocket)
        val r = min(w, h) * .035f

        // Corner pockets
        val pocketPaint = Paint(3).apply { color = Color.rgb(35, 28, 24) }
        val pr = r * 1.9f
        listOf(
            PointF(inner.left, inner.top), PointF(inner.right, inner.top),
            PointF(inner.left, inner.bottom), PointF(inner.right, inner.bottom)
        ).forEach { canvas.drawCircle(it.x, it.y, pr, pocketPaint) }

        // Baselines and center circle
        val decor = Paint(3).apply { color = Color.rgb(150, 70, 45); style = Paint.Style.STROKE; strokeWidth = 3f }
        canvas.drawCircle(inner.centerX(), inner.centerY(), min(w,h)*.13f, decor)
        canvas.drawLine(inner.left + w*.12f, inner.top+h*.18f, inner.right-w*.12f, inner.top+h*.18f, decor)
        canvas.drawLine(inner.left + w*.12f, inner.bottom-h*.18f, inner.right-w*.12f, inner.bottom-h*.18f, decor)

        // Target-to-pocket and striker-to-target guides
        canvas.drawLine(t.x, t.y, p.x, p.y, dashed)
        canvas.drawLine(s.x, s.y, t.x, t.y, line)
        val arrow = Paint(3).apply { color = Color.rgb(35,105,220); strokeWidth=5f }
        val a = atan2((t.y-s.y).toDouble(), (t.x-s.x).toDouble())
        val ah = 22f
        canvas.drawLine(t.x, t.y, t.x-ah*cos(a-.5).toFloat(), t.y-ah*sin(a-.5).toFloat(), arrow)
        canvas.drawLine(t.x, t.y, t.x-ah*cos(a+.5).toFloat(), t.y-ah*sin(a+.5).toFloat(), arrow)

        canvas.drawCircle(s.x, s.y, r*1.2f, Paint(3).apply { color = Color.WHITE })
        canvas.drawCircle(s.x, s.y, r*.65f, Paint(3).apply { color = Color.rgb(65,125,210) })
        canvas.drawCircle(t.x, t.y, r, Paint(3).apply { color = Color.rgb(210,55,55) })
        canvas.drawCircle(p.x, p.y, pr*.55f, Paint(3).apply { color = Color.BLACK })
        canvas.drawText("STRIKER", inner.left+12, inner.bottom-16, textPaint.apply { textSize=22f })
        if (practiceProgress > 0f) {
            canvas.drawText("PRACTICE SHOT", inner.centerX()-100, inner.centerY()+h*.28f, textPaint)
            practiceProgress = 0f
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        val pad = 54f
        val iw = (width - 2*pad).coerceAtLeast(1f)
        val ih = (height - 2*pad).coerceAtLeast(1f)
        fun norm(x: Float, y: Float) = PointF(((x-pad)/iw).coerceIn(.04f,.96f), ((y-pad)/ih).coerceIn(.04f,.96f))
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                lastTouch = norm(event.x, event.y); dragging = true; striker = lastTouch
                invalidate(); return true
            }
            android.view.MotionEvent.ACTION_MOVE -> if (dragging) {
                target = norm(event.x, event.y); invalidate(); return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                dragging = false
                val dx = target.x - striker.x; val dy = target.y - striker.y
                val angle = Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble()))
                val distance = hypot(dx.toDouble(), dy.toDouble())
                val power = (35 + distance * 65).coerceIn(20.0, 100.0)
                onAim(angle, power); invalidate(); return true
            }
        }
        return true
    }
}
