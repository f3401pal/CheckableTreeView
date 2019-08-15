package com.f3401pal.checkabletreeview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatCheckBox

class CheckBoxEx : AppCompatCheckBox {

    private var isIndeterminate = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initCustomAttributes(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style) {
        initCustomAttributes(attributeSet)
    }

    fun isIndeterminate(): Boolean {
        return isIndeterminate
    }

    fun setIndeterminate(isIndeterminate: Boolean) {
        this.isIndeterminate = isIndeterminate
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if(isIndeterminate) View.mergeDrawableStates(drawableState, STATE_INDETERMINATE)

        return drawableState
    }

    private fun initCustomAttributes(attributeSet: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.CheckBoxEx)
        isIndeterminate = typeArray.getBoolean(R.styleable.CheckBoxEx_state_indeterminate, false)

        typeArray.recycle()
    }

    companion object {

        private val STATE_INDETERMINATE = intArrayOf(R.attr.state_indeterminate)

    }
}