package com.f3401pal.checkabletreeview

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    fun `the node is a root node if its parent is null`() {
        Assert.assertTrue(root.isTop())
    }

    @Test
    fun `the node is a leaf node if its child is empty`() {
        Assert.assertTrue(right.isLeaf())
    }

    @Test
    fun `set all children to be checked when their parent is checked`() {
        root.setChecked(true)
        Assert.assertTrue(root.getCheckedStatus().allChildrenChecked)
    }

    @Test
    fun `set parent node checked when all its children are checked`() {
        left.setChecked(true)
        right.setChecked(true)

        Assert.assertTrue(root.getCheckedStatus().allChildrenChecked)
    }

    @Test
    fun `all children is checked return correct value in sequence`() {
        root.setChecked(true)
        left.getChildren()[1].setChecked(false)

        Assert.assertFalse(root.getCheckedStatus().allChildrenChecked)

        right.setChecked(false)
        Assert.assertFalse(root.getCheckedStatus().allChildrenChecked)

        left.getChildren()[0].setChecked(false)
        Assert.assertFalse(root.getCheckedStatus().allChildrenChecked)
    }

    @Test
    fun `has child check returns correct value in sequence`() {
        root.setChecked(true)
        left.getChildren()[1].setChecked(false)

        Assert.assertTrue(root.getCheckedStatus().hasChildChecked)

        right.setChecked(false)
        Assert.assertFalse(right.getCheckedStatus().hasChildChecked)

        left.getChildren()[0].setChecked(false)
        Assert.assertFalse(root.getCheckedStatus().hasChildChecked)
    }

    @Test
    fun `has child check is true if one of the child is checked`() {
        left.getChildren()[0].setChecked(true)
        Assert.assertTrue(root.getCheckedStatus().hasChildChecked)
    }

    @Test
    fun `aggregated value returns the parent node text if all its children are checked`() {
        root.setChecked(true)

        Assert.assertEquals(1, root.getAggregatedValues().size)
        Assert.assertEquals("root", root.getAggregatedValues()[0].str)
    }

    @Test
    fun `aggregated value returns the checked child node text in depth`() {
        left.getChildren()[0].setChecked(true)

        Assert.assertEquals(1, root.getAggregatedValues().size)
        Assert.assertEquals("level3left", root.getAggregatedValues()[0].str)
    }

    @Test
    fun `aggregated value returns ONLY the checked child node text if NOT all its children are checked`() {
        left.setChecked(true)

        Assert.assertEquals(1, root.getAggregatedValues().size)
        Assert.assertEquals("left", root.getAggregatedValues()[0].str)
    }

    @Test
    fun `nodes with depth 3 has level 2`() {
        Assert.assertEquals(2, left.getChildren()[0].getLevel())
    }

    @Test
    fun `root node has level 0`() {
        Assert.assertEquals(0, root.getLevel())
    }
}