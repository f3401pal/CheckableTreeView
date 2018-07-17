package com.f3401pal.checkabletreeview

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TreeNodeTest {

    private lateinit var left: TreeNode<StringNode>
    private lateinit var right: TreeNode<StringNode>
    private lateinit var root: TreeNode<StringNode>

    @Before
    fun setUp() {
        root = TreeNode(StringNode("root"))
        left = TreeNode(StringNode("left"), root).apply {
            setChildren(listOf(TreeNode(StringNode("level3left"), this),
                    TreeNode(StringNode("level3right"), this)))
        }
        right = TreeNode(StringNode("right"), root)

        root.setChildren(listOf(left, right))
    }

    @Test
    fun isTop_returnsTrue_ifParentIsNull() {
        Assert.assertTrue(root.isTop())
    }

    @Test
    fun isLeaf_returnTrue_ifChildrenIsEmpty() {
        Assert.assertTrue(right.isLeaf())
    }

    @Test
    fun setChecked_setAllChildrenToChecked() {
        root.setChecked(true)
        Assert.assertTrue(root.getCheckedStatus().allChildrenChecked)
    }

    @Test
    fun setChecked_setParentChecked() {
        left.setChecked(true)
        right.setChecked(true)

        Assert.assertTrue(root.getCheckedStatus().allChildrenChecked)
    }

    @Test
    fun setChecked_complexSequence() {
        root.setChecked(true)
        left.getChildren()[1].setChecked(false)

        Assert.assertFalse(root.getCheckedStatus().allChildrenChecked)
        Assert.assertTrue(root.getCheckedStatus().hasChildChecked)

        right.setChecked(false)
        Assert.assertFalse(root.getCheckedStatus().allChildrenChecked)
        Assert.assertFalse(right.getCheckedStatus().hasChildChecked)

        left.getChildren()[0].setChecked(false)
        Assert.assertFalse(root.getCheckedStatus().allChildrenChecked)
        Assert.assertFalse(root.getCheckedStatus().hasChildChecked)
    }

    @Test
    fun getCheckedStatus_returnAllChildrenCheckedTrue() {
        root.setChecked(true)
        Assert.assertTrue(root.getCheckedStatus().allChildrenChecked)
    }

    @Test
    fun getCheckedStatus_returnHasChildCheckedTrue() {
        left.getChildren()[0].setChecked(true)
        Assert.assertTrue(root.getCheckedStatus().hasChildChecked)
    }

    @Test
    fun getAggregatedValues_returnRootValue_ifAllChecked() {
        root.setChecked(true)

        Assert.assertEquals(1, root.getAggregatedValues().size)
        Assert.assertEquals("root", root.getAggregatedValues()[0].str)
    }

    @Test
    fun getAggregatedValues_returnCheckedValues() {
        left.getChildren()[0].setChecked(true)

        Assert.assertEquals(1, root.getAggregatedValues().size)
        Assert.assertEquals("level3left", root.getAggregatedValues()[0].str)
    }

    @Test
    fun getAggregatedValues_returnAggregatedValue() {
        left.setChecked(true)

        Assert.assertEquals(1, root.getAggregatedValues().size)
        Assert.assertEquals("left", root.getAggregatedValues()[0].str)
    }

    @Test
    fun getLevel_returns2_onLevel3Node() {
        Assert.assertEquals(2, left.getChildren()[0].getLevel())
    }

    @Test
    fun getLevel_returns0_onRootNode() {
        Assert.assertEquals(0, root.getLevel())
    }
}