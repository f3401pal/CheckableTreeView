package com.f3401pal.checkabletreeview

private const val DEFAULT_INDENTATION_IN_DP = 16

interface CheckableTreeView<T : Checkable> {

    val indentation: Int
        get() = DEFAULT_INDENTATION_IN_DP

    fun setRoots(roots: List<TreeNode<T>>)
}

data class NodeCheckedStatus(val hasChildChecked: Boolean, val allChildrenChecked: Boolean)

open class Checkable(internal var checked: Boolean)

interface HasId {
    val id: Long
}

interface Expandable {
    var isExpanded: Boolean
}