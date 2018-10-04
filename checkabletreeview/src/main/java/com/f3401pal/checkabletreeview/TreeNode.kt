package com.f3401pal.checkabletreeview


object TreeNodeFactory {

    fun buildTestTree(): TreeNode<StringNode> {
        val root = TreeNode(StringNode("root"))
        val left = TreeNode(StringNode("left"), root).apply {
            setChildren(listOf(TreeNode(StringNode("level3left"), this),
                    TreeNode(StringNode("level3right"), this)))
        }
        val right = TreeNode(StringNode("right"), root)

        root.setChildren(listOf(left, right))
        return root
    }
}

data class StringNode(val str: String) : Checkable(false) {
    override fun toString(): String {
        return str
    }
}

class TreeNode<T : Checkable> : HasId {

    override val id: Long by lazy {
        IdGenerator.generate()
    }

    private val value: T

    private var parent: TreeNode<T>?
    private var children: List<TreeNode<T>>

    constructor(value: T, parent: TreeNode<T>?, children: List<TreeNode<T>>) {
        this.value = value
        this.parent = parent
        this.children = children
    }

    // constructor for root node
    constructor(value: T) : this(value, null, emptyList())

    // constructor for leaf node
    constructor(value: T, parent: TreeNode<T>) : this(value, parent, emptyList())

    // constructor for parent node
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