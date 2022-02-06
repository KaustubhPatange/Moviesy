package com.kpstv.yts.extensions

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

// In order for this to work the recycler item views must be of fixed width.
class GridAutoSpacingItemDecoration(
    private val containerWidth: Int,
    private val childWidth: Int,
    private val spanCount: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (includeEdge) {
            val marginHorizontal = abs(containerWidth - (childWidth * spanCount)) / (spanCount + 1)
            if (position % spanCount == 0) {
                outRect.left = marginHorizontal
            } else {
                outRect.left = marginHorizontal / 2
            }
            if ((position + 1) % spanCount == 0) {
                outRect.right = marginHorizontal
            } else {
                outRect.right = marginHorizontal / 2
            }
        } else {
            val marginHorizontal = abs(containerWidth - (childWidth * spanCount)) / (spanCount - 1)

            if ((position + 1) % spanCount != 0) {
                outRect.right = marginHorizontal / 2
            }

            if (position % spanCount != 0) {
                outRect.left = marginHorizontal / 2
            }
        }
    }

    companion object {
        fun calculateSpanCount(containerWidth: Int, childWidth: Int, minSpacing: Int) : Int {
            return (containerWidth + minSpacing) / (childWidth + minSpacing)
        }
    }
}
