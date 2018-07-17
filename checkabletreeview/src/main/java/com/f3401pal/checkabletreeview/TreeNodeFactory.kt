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