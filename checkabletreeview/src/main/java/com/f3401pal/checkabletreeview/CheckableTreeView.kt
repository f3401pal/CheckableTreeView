package com.f3401pal.checkabletreeview

private const val DEFAULT_INDENTATION_IN_DP = 16

interface CheckableTreeView<T : Checkable> {

    val indentation: Int
        get() = DEFAULT_INDENTATION_IN_DP

    fun setRoots(roots: List<TreeNode<T>>)
}

open class TreeNode<T : Checkable> : HasId {

    override val id: Long by lazy {
        IdGenerator.generate()
    }

    private val parent: TreeNode<T>?
    private val value: T
    private var children: List<TreeNode<T>>

    constructor(value: T, parent: TreeNode<T>?, children: List<TreeNode<T>>) {
        this.value = value
        this.parent = parent
        this.children = children ?: emptyList()
    }

    // constructor for root node
    constructor(value: T) : this(value, null, emptyList())

    // constructor for leaf node
    constructor(value: T, parent: TreeNode<T>) : this(value, parent, emptyList())

    // constructor for top parent node
    constructor(value: T, children: List<TreeNode<T>>) : this(value, null, children)

    fun isTop(): Boolean {
        return parent == null
    }

    fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    fun getValue(): T {
        return value
    }

    fun getLevel(): Int {
        fun stepUp (node: TreeNode<T>): Int {
            return node.parent?.let { 1 + stepUp(it) } ?: 0
        }
        return stepUp(this)
    }

    fun setChildren(children: List<TreeNode<T>>) {
        this.children = children
    }

    fun getChildren(): List<TreeNode<T>> {
        return children
    }

    fun setChecked(isChecked: Boolean) {
        value.checked = isChecked
        // cascade the action to children
        children.forEach {
            it.setChecked(isChecked)
        }
    }

    fun getCheckedStatus(): NodeCheckedStatus {
        if (isLeaf()) return NodeCheckedStatus(value.checked, value.checked)
        var hasChildChecked = false
        var allChildrenChecked = true
        children.forEach {
            val checkedStatus = it.getCheckedStatus()
            hasChildChecked = hasChildChecked || checkedStatus.hasChildChecked
            allChildrenChecked = allChildrenChecked && checkedStatus.allChildrenChecked
        }
        return NodeCheckedStatus(hasChildChecked, allChildrenChecked)
    }

    fun getAggregatedValues(): List<T> {
        return if (isLeaf()) {
            if (value.checked) listOf(value) else emptyList()
        } else {
            if (getCheckedStatus().allChildrenChecked) {
                listOf(value)
            } else {
                val result = mutableListOf<T>()
                children.forEach {
                    result.addAll(it.getAggregatedValues())
                }
                result
            }
        }
    }
}

data class NodeCheckedStatus(val hasChildChecked: Boolean, val allChildrenChecked: Boolean)

open class Checkable(internal var checked: Boolean)

interface HasId {
    val id: Long
}