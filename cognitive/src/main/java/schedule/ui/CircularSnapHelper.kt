package schedule.ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class CircularSnapHelper(
    private val originalSize: Int,  // 原始数据大小，如 24（小时）或 60（分钟）
    loopMultiplier: Int = 200
) : LinearSnapHelper() {

    private val middlePosition = originalSize * loopMultiplier / 2
    private val totalSize = originalSize * loopMultiplier

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if (layoutManager !is LinearLayoutManager) {
            return RecyclerView.NO_POSITION
        }

        // 1. 先让父类计算目标位置
        val targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        // 2. 将目标位置映射到中间区域附近
        return normalizeToMiddleRegion(targetPos)
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        // 调用父类计算距离
        val distance = super.calculateDistanceToFinalSnap(layoutManager, targetView) ?: return intArrayOf(0, 0)

        // 确保视图完全对齐到中心
        val layout = layoutManager as? LinearLayoutManager ?: return distance
        val childCenter = getChildCenter(layout, targetView)
        val parentCenter = if (layout.orientation == LinearLayoutManager.VERTICAL) {
            layout.height / 2
        } else {
            layout.width / 2
        }

        if (layout.orientation == LinearLayoutManager.VERTICAL) {
            distance[1] = childCenter - parentCenter
        } else {
            distance[0] = childCenter - parentCenter
        }

        return distance
    }

    private fun getChildCenter(layout: LinearLayoutManager, view: View): Int {
        return if (layout.orientation == LinearLayoutManager.VERTICAL) {
            view.top + view.height / 2
        } else {
            view.left + view.width / 2
        }
    }

    private fun normalizeToMiddleRegion(position: Int): Int {
        // 计算相对于中间位置的偏移
        val realPos = (position % originalSize + originalSize) % originalSize
        // 返回中间区域的位置
        return middlePosition + realPos
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        val snapView = super.findSnapView(layoutManager)
        // 如果找不到，尝试找到最接近中心的视图
        if (snapView == null && layoutManager is LinearLayoutManager) {
            return findViewNearestCenter(layoutManager)
        }
        return snapView
    }

    private fun findViewNearestCenter(layoutManager: LinearLayoutManager): View? {
        val orientation = layoutManager.orientation
        val center = if (orientation == LinearLayoutManager.VERTICAL) {
            layoutManager.height / 2
        } else {
            layoutManager.width / 2
        }

        var minDistance = Int.MAX_VALUE
        var nearestView: View? = null

        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val childCenter = if (orientation == LinearLayoutManager.VERTICAL) {
                child.top + child.height / 2
            } else {
                child.left + child.width / 2
            }
            val distance = abs(childCenter - center)
            if (distance < minDistance) {
                minDistance = distance
                nearestView = child
            }
        }
        return nearestView
    }
}
