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

    // 使用 lateinit 并在需要时确保初始化，避免在父类构造期间 NPE
    private lateinit var hanziList: MutableList<String>
    private lateinit var pinyinList: MutableList<String>

    val verticalSpacing = 9f

    private val pinyinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // 注意：textSize 可能在构造早期还是默认值，运行时会根据实际 textSize 调整也可通过方法暴露给外部设置
        textSize = this@PinyinTextView.textSize * 0.6f
        color = currentTextColor
    }

    // 每次 setText 时确保列表已经初始化再解析
    override fun setText(text: CharSequence?, type: BufferType?) {
        // 如果列表还没初始化，先初始化（这避免了父类构造期间 setText 导致的 NPE）
        if (!::hanziList.isInitialized) {
            hanziList = mutableListOf()
            pinyinList = mutableListOf()
        }

        if (!isInEditMode && !text.isNullOrEmpty()) {
            parseHanzi(text.toString())
        } else {
            // 若文本为空，则清空列表
            hanziList.clear()
            pinyinList.clear()
        }

        super.setText(text, type)
        requestLayout()
        invalidate()
    }

    // 用 pinyin4j 生成拼音（示例：带声调符号）
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
                // 非汉字（空格/标点/英文字母），拼音置空以保持布局一致
                hanziList.add(s)
                pinyinList.add("")
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 如果列表未初始化或为空，回退给父类绘制（兼容编辑器预览）
        if (!::hanziList.isInitialized || hanziList.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        // 计算高度相关
        val hanziFont = paint
        val hanziMetrics = hanziFont.fontMetrics
        val hanziHeight = hanziMetrics.bottom - hanziMetrics.top

        val pinyinMetrics = pinyinPaint.fontMetrics
        val pinyinHeight = pinyinMetrics.bottom - pinyinMetrics.top

        val lineSpace = 8f   // 行间额外间距
        val wordSpace = 12f  // 字之间额外水平间距

        val contentWidth = width - paddingLeft - paddingRight

        var x = paddingLeft.toFloat()
        // y 指向拼音的 baseline（选择一个合适的初始 y）
        var y = paddingTop - pinyinMetrics.top

        for (i in hanziList.indices) {
            val h = hanziList[i]
            val p = pinyinList[i]

            val hanziW = hanziFont.measureText(h)
            val pinyinW = pinyinPaint.measureText(p)
            val cellW = maxOf(hanziW, pinyinW)

            // 换行判断（注意 contentWidth 为可用宽度）
            if (x + cellW > paddingLeft + contentWidth) {
                x = paddingLeft.toFloat()
                y += pinyinHeight + hanziHeight + lineSpace
            }

            // 画拼音（如果有）
            if (p.isNotEmpty()) {
                val px = x + (cellW - pinyinW) / 2f
                canvas.drawText(p, px, y, pinyinPaint)
            }

            // 画汉字（在拼音下方）
            val hx = x + (cellW - hanziW) / 2f
            canvas.drawText(h, hx, y + pinyinHeight + verticalSpacing, hanziFont)

            x += cellW + wordSpace
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val contentWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight

        // 初始 x, y
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
