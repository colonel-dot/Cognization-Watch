package ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

class PinyinTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private lateinit var hanziList: MutableList<String>
    private lateinit var pinyinList: MutableList<String>

    val verticalSpacing = 9f

    private val pinyinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = this@PinyinTextView.textSize * 0.6f
        color = currentTextColor
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (!::hanziList.isInitialized) {
            hanziList = mutableListOf()
            pinyinList = mutableListOf()
        }

        if (!isInEditMode && !text.isNullOrEmpty()) {
            parseHanzi(text.toString())
        } else {
            hanziList.clear()
            pinyinList.clear()
        }

        super.setText(text, type)
        requestLayout()
        invalidate()
    }

    private fun parseHanzi(text: String) {
        hanziList.clear()
        pinyinList.clear()

        val format = HanyuPinyinOutputFormat().apply {
            caseType = HanyuPinyinCaseType.LOWERCASE
            toneType = HanyuPinyinToneType.WITH_TONE_MARK
            vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
        }

        for (c in text) {
            val s = c.toString()
            if (s.matches("[\\u4E00-\\u9FA5]".toRegex())) {
                try {
                    val arr = PinyinHelper.toHanyuPinyinStringArray(c, format)
                    val py = arr?.firstOrNull() ?: ""
                    pinyinList.add(py)
                } catch (e: Exception) {
                    pinyinList.add("")
                }
                hanziList.add(s)
            } else {
                hanziList.add(s)
                pinyinList.add("")
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!::hanziList.isInitialized || hanziList.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        val hanziFont = paint
        val hanziMetrics = hanziFont.fontMetrics
        val hanziHeight = hanziMetrics.bottom - hanziMetrics.top

        val pinyinMetrics = pinyinPaint.fontMetrics
        val pinyinHeight = pinyinMetrics.bottom - pinyinMetrics.top

        val lineSpace = 8f
        val wordSpace = 12f

        val contentWidth = width - paddingLeft - paddingRight

        var x = paddingLeft.toFloat()
        var y = paddingTop - pinyinMetrics.top

        for (i in hanziList.indices) {
            val h = hanziList[i]
            val p = pinyinList[i]

            val hanziW = hanziFont.measureText(h)
            val pinyinW = pinyinPaint.measureText(p)
            val cellW = maxOf(hanziW, pinyinW)

            if (x + cellW > paddingLeft + contentWidth) {
                x = paddingLeft.toFloat()
                y += pinyinHeight + hanziHeight + lineSpace
            }

            if (p.isNotEmpty()) {
                val px = x + (cellW - pinyinW) / 2f
                canvas.drawText(p, px, y, pinyinPaint)
            }

            val hx = x + (cellW - hanziW) / 2f
            canvas.drawText(h, hx, y + pinyinHeight + verticalSpacing, hanziFont)

            x += cellW + wordSpace
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val contentWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight

        var x = 0f
        var y = 0f
        val lineSpace = 8f
        val wordSpace = 12f

        val hanziFont = paint
        val hanziHeight = hanziFont.fontMetrics.bottom - hanziFont.fontMetrics.top
        val pinyinHeight = pinyinPaint.fontMetrics.bottom - pinyinPaint.fontMetrics.top

        var totalHeight = 0f
        for (i in hanziList.indices) {
            val hanziW = hanziFont.measureText(hanziList[i])
            val pinyinW = pinyinPaint.measureText(pinyinList[i])
            val cellW = maxOf(hanziW, pinyinW)

            if (x + cellW > contentWidth) {
                x = 0f
                y += hanziHeight + pinyinHeight + lineSpace
            }

            x += cellW + wordSpace
            totalHeight = y + hanziHeight + pinyinHeight
        }

        val finalHeight = (totalHeight + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            resolveSize(finalHeight, heightMeasureSpec)
        )
    }
}