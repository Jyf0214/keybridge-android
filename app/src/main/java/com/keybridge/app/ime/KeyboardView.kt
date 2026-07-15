package com.keybridge.app.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.keybridge.app.data.KeyData
import com.keybridge.app.data.KeyType

/**
 * 自定义键盘 View
 * 用传统 Canvas 绘制按键，兼容所有 Android 版本
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onKeyClick: ((KeyData) -> Unit)? = null
    var onTabClick: ((Int) -> Unit)? = null

    // 键盘布局数据
    private var rows: List<List<KeyData>> = emptyList()

    // 绘制工具
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val keyStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCCCCC")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val activeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val activeKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1565C0")
        style = Paint.Style.FILL
    }
    private val specialKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D8D8D8")
        style = Paint.Style.FILL
    }
    private val pressedKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BBDDFF")
        style = Paint.Style.FILL
    }

    // 按键尺寸
    private val keyMargin = 4f
    private val keyCornerRadius = 12f
    private val keyHeight = 90f
    private val keyRect = RectF()

    // 按键区域列表（用于触摸检测）
    private data class KeyRect(val rect: RectF, val keyData: KeyData)
    private var keyRects: MutableList<KeyRect> = mutableListOf()
    private var pressedKey: KeyData? = null

    // 当前激活的修饰键
    private var activeModifiers: MutableSet<Int> = mutableSetOf()

    // 当前页面
    var currentPage: Int = 0
        private set

    fun setKeyboardLayout(rows: List<List<KeyData>>) {
        this.rows = rows
        requestLayout()
        invalidate()
    }

    fun setPage(pageIndex: Int) {
        currentPage = pageIndex
        invalidate()
    }

    fun setActiveModifiers(modifiers: Set<Int>) {
        activeModifiers = modifiers.toMutableSet()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val totalHeight = (rows.size * (keyHeight + keyMargin * 2) + keyMargin * 2).toInt()
        setMeasuredDimension(width, totalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        keyRects = mutableListOf()

        if (rows.isEmpty()) return

        val totalWidth = width.toFloat()
        val startX = keyMargin

        rows.forEachIndexed { rowIndex, row ->
            val y = keyMargin + rowIndex * (keyHeight + keyMargin * 2)
            val totalWeight = row.sumOf { it.widthWeight.toDouble() }.toFloat()
            val availableWidth = totalWidth - keyMargin * 2
            var currentX = startX

            row.forEach { keyData ->
                val keyWidth = (availableWidth * keyData.widthWeight / totalWeight) - keyMargin * 2
                val rect = RectF(
                    currentX + keyMargin,
                    y,
                    currentX + keyMargin + keyWidth,
                    y + keyHeight
                )

                // 绘制按键背景
                val isPressed = pressedKey?.let {
                    it.label == keyData.label && it.keyCode == keyData.keyCode
                } ?: false

                val isActive = activeModifiers.contains(keyData.keyCode)

                val paint = when {
                    isPressed -> pressedKeyPaint
                    isActive -> activeTextPaint
                    keyData.type == KeyType.MODIFIER || keyData.type == KeyType.SPECIAL -> specialKeyPaint
                    else -> keyPaint
                }

                // 圆角矩形
                keyRect.set(rect)
                canvas.drawRoundRect(keyRect, keyCornerRadius, keyCornerRadius, paint)
                canvas.drawRoundRect(keyRect, keyCornerRadius, keyCornerRadius, keyStrokePaint)

                // 绘制文字
                val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
                val textToDraw = keyData.label

                val p = if (isActive) activeTextPaint else textPaint
                // 调整文字大小
                p.textSize = when {
                    textToDraw.length > 3 -> 24f
                    textToDraw.length > 2 -> 28f
                    else -> 36f
                }

                // 修饰键高亮时文字变白
                if (isActive) {
                    p.color = Color.WHITE
                } else {
                    p.color = when {
                        keyData.type == KeyType.MODIFIER -> Color.parseColor("#333333")
                        keyData.type == KeyType.SPECIAL -> Color.parseColor("#1565C0")
                        else -> Color.parseColor("#333333")
                    }
                }

                canvas.drawText(textToDraw, rect.centerX(), textY, p)

                keyRects.add(KeyRect(rect, keyData))
                currentX += keyWidth + keyMargin * 2
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val key = getKeyAt(event.x, event.y)
                if (key != null) {
                    pressedKey = key
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                val key = pressedKey
                pressedKey = null
                invalidate()

                if (key != null) {
                    val upKey = getKeyAt(event.x, event.y)
                    if (upKey != null && upKey.label == key.label && upKey.keyCode == key.keyCode) {
                        onKeyClick?.invoke(key)
                    }
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getKeyAt(x: Float, y: Float): KeyData? {
        for (keyRect in keyRects) {
            if (keyRect.rect.contains(x, y)) {
                return keyRect.keyData
            }
        }
        return null
    }
}
