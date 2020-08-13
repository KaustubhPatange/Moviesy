package com.kpstv.yts.extensions.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class ExtendedNestedScrollView : NestedScrollView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributes,
        defStyleAttr
    )

    public override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }
}