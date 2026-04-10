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

        val targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        return normalizeToMiddleRegion(targetPos)
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val distance = super.calculateDistanceToFinalSnap(layoutManager, targetView) ?: return intArrayOf(0, 0)

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
        val realPos = (position % originalSize + originalSize) % originalSize
        return middlePosition + realPos
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        val snapView = super.findSnapView(layoutManager)
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
