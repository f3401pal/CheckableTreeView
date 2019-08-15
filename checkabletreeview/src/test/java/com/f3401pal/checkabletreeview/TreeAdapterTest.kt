package com.f3401pal.checkabletreeview

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.item_checkable_text.view.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TreeAdapterTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val viewGroup = RecyclerView(context).apply {
        layoutManager = LinearLayoutManager(context)
    }
    private lateinit var adapter: TreeAdapter<StringNode>

    @Before
    fun setUp() {
        adapter = TreeAdapter(10)
    }

    @Test
    fun `the adapter has stable ID by default`() {
        assertTrue(adapter.hasStableIds())
    }

    @Test
    fun `the adapter is empty on initialization`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `the adapter creates View holder that is not null`() {
        val viewHolder = adapter.createViewHolder(viewGroup, 0)

        assertNotNull(viewHolder)
    }

    @Test
    fun `the adapter size should be the number of root node`() {
        val nodes = TreeNodeFactory.buildTestTree()
        adapter.nodes.add(nodes)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `the adapter should bind the ViewHolder to a node`() {
        val nodes = TreeNodeFactory.buildTestTree()
        adapter.nodes.add(nodes)
        val viewHolder: TreeAdapter<StringNode>.ViewHolder = mockk(relaxUnitFun = true)

        adapter.onBindViewHolder(viewHolder, 0)

        verify {
            viewHolder.bind(nodes)
        }
    }

    @Test
    fun `child node should be visible when expand on parent node`() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            nodes.add(node)
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)
            viewHolder.itemView.expandIndicator.performClick()
        }

        assertEquals(3, adapter.itemCount)
        assertArrayEquals(
                node.getChildren().toTypedArray(),
                adapter.nodes.subList(1, adapter.nodes.size).toTypedArray()
        )
    }

    @Test
    fun `the node should be set to expanded when expended`() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            nodes.add(node)
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)
            viewHolder.itemView.expandIndicator.performClick()
        }

        assertTrue(node.isExpanded)
    }

    @Test
    fun `child node should be hidden when collapse on parent node`() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            node.isExpanded = true
            nodes.add(node)
            nodes.addAll(node.getChildren())
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)

            assertEquals(3, adapter.itemCount)

            viewHolder.itemView.expandIndicator.performClick()
        }

        assertEquals(1, adapter.itemCount)
        assertArrayEquals(arrayOf(node), adapter.nodes.toTypedArray())
    }

    @Test
    fun `the node should be set to NOT expanded when collapse`() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            node.isExpanded = true
            nodes.add(node)
            nodes.addAll(node.getChildren())
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)

            assertEquals(3, adapter.itemCount)

            viewHolder.itemView.expandIndicator.performClick()
        }

        assertFalse(node.isExpanded)
    }
}

@RunWith(AndroidJUnit4::class)
class ViewHolderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val viewGroup = RecyclerView(context).apply {
        layoutManager = LinearLayoutManager(context)
    }
    private val adapter: TreeAdapter<StringNode> = TreeAdapter(10)

    private lateinit var subject: TreeAdapter<StringNode>.ViewHolder

    private val parentNode = TreeNode(StringNode("root"))
    private val leafNode = TreeNode(StringNode("leaf"), parentNode).apply {
        parentNode.setChildren(listOf(this))
    }

    @Before
    fun setUp() {
        subject = adapter.createViewHolder(viewGroup, 0)
    }

    @Test
    fun `expand indicator on leaf nodes should be hidden`() {
        subject.bind(leafNode)

        assertEquals(View.GONE, subject.itemView.expandIndicator.visibility)
    }

    @Test
    fun `expand indicator on nodes with children should be shown`() {
        subject.bind(parentNode)

        assertEquals(View.VISIBLE, subject.itemView.expandIndicator.visibility)
    }

    @Test
    fun `the item is indeterminate but NOT checked when only some its children are checked`() {
        val status = NodeCheckedStatus(hasChildChecked = true, allChildrenChecked = false)
        spyk(parentNode).run {
            every { getCheckedStatus() } returns status
            subject.bind(this)
        }

        assertFalse(subject.itemView.checkText.isChecked)
        assertTrue(subject.itemView.checkText.isIndeterminate())
    }

    @Test
    fun `the item is both checked and indeterminate when all its children are checked`() {
        val status = NodeCheckedStatus(hasChildChecked = true, allChildrenChecked = true)
        spyk(parentNode).run {
            every { getCheckedStatus() } returns status
            subject.bind(this)
        }

        assertTrue(subject.itemView.checkText.isChecked)
        assertTrue(subject.itemView.checkText.isIndeterminate())
    }

    @Test
    fun `the checkbox text should be the node text`() {
        subject.bind(leafNode)

        assertEquals(leafNode.getValue().str, subject.itemView.checkText.text)
    }

    @Test
    fun `the start indentation should be 10 on level 1 node`() {
        subject.bind(leafNode)

        assertEquals(10, subject.itemView.indentation.minimumWidth)
    }
}