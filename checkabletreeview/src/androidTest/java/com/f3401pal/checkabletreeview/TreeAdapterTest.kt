package com.f3401pal.checkabletreeview

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
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

    private val context = InstrumentationRegistry.getTargetContext()
    private val viewGroup = RecyclerView(context).apply {
        layoutManager = LinearLayoutManager(context)
    }
    private lateinit var adapter: TreeAdapter<StringNode>

    @Before
    fun setUp() {
        adapter = TreeAdapter(10)
    }

    @Test
    fun init_hasStableIds_true() {
        assertTrue(adapter.hasStableIds())
    }

    @Test
    fun init_isEmpty_true() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun onCreateViewHolder_returnsCorrectViewHolder() {
        val viewHolder = adapter.createViewHolder(viewGroup, 0)

        assertNotNull(viewHolder)
    }

    @Test
    fun getItemCount_returnsSizeOfNodes() {
        val nodes = TreeNodeFactory.buildTestTree()
        adapter.nodes.add(nodes)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun onBindViewHolder_bindCorrectNode() {
        val nodes = TreeNodeFactory.buildTestTree()
        adapter.nodes.add(nodes)
        val viewHolder: TreeAdapter<StringNode>.ViewHolder = mockk(relaxUnitFun = true)

        adapter.onBindViewHolder(viewHolder, 0)

        verify {
            viewHolder.bind(nodes)
        }
    }

    @Test
    fun expand_addChildNodesToTheDisplayList() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            nodes.add(node)
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)
            viewHolder.itemView.expandIndicator.performClick()
        }

        assertEquals(3, adapter.itemCount)
        assertArrayEquals(adapter.nodes.subList(1, adapter.nodes.size).toTypedArray(),
                node.getChildren().toTypedArray())
    }

    @Test
    fun expand_addNodeIdToExpendedNodeList() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            nodes.add(node)
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)
            viewHolder.itemView.expandIndicator.performClick()
        }

        assertTrue(adapter.expandedNodeIds.contains(node.id))
    }

    @Test
    fun collapse_removeChildNodesToTheDisplayList() {
        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            expandedNodeIds.add(node.id)
            nodes.add(node)
            nodes.addAll(node.getChildren())
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)

            assertTrue(adapter.expandedNodeIds.contains(node.id))
            assertEquals(3, adapter.itemCount)

            viewHolder.itemView.expandIndicator.performClick()
        }

        assertEquals(1, adapter.itemCount)
        assertArrayEquals(adapter.nodes.toTypedArray(), arrayOf(node))
    }

    @Test
    fun collapse_removeNodeIdToExpendedNodeList() {

        val node = TreeNodeFactory.buildTestTree()
        with(adapter) {
            expandedNodeIds.add(node.id)
            nodes.add(node)
            nodes.addAll(node.getChildren())
            val viewHolder = spyk(createViewHolder(viewGroup, 0))
            every { viewHolder.adapterPosition } returns 0
            bindViewHolder(viewHolder, 0)

            assertTrue(adapter.expandedNodeIds.contains(node.id))
            assertEquals(3, adapter.itemCount)

            viewHolder.itemView.expandIndicator.performClick()
        }

        assertTrue(adapter.expandedNodeIds.isEmpty())

    }
}

@RunWith(AndroidJUnit4::class)
class ViewHolderTest {

    private val context = InstrumentationRegistry.getTargetContext()
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
    fun bind_setExpandIndicatorForLeafNode() {
        subject.bind(leafNode)

        assertEquals(View.GONE, subject.itemView.expandIndicator.visibility)
    }

    @Test
    fun bind_setExpandIndicatorForParentNode() {
        subject.bind(parentNode)

        assertEquals(View.VISIBLE, subject.itemView.expandIndicator.visibility)
    }

    @Test
    fun bind_setCheckboxIndeterminate_ifHasChildrenCheckAndNotAllChildrenAreChecked() {
        val status = NodeCheckedStatus(true, false)
        spyk(parentNode).run {
            every { getCheckedStatus() } returns status
            subject.bind(this)
        }

        assertFalse(subject.itemView.checkText.isChecked)
        assertTrue(subject.itemView.checkText.isIndeterminate())
    }

    @Test
    fun bind_setCheckboxChecked_ifAllChildrenAreChecked() {
        val status = NodeCheckedStatus(true, true)
        spyk(parentNode).run {
            every { getCheckedStatus() } returns status
            subject.bind(this)
        }

        assertTrue(subject.itemView.checkText.isChecked)
        assertTrue(subject.itemView.checkText.isIndeterminate())
    }

    @Test
    fun bind_setCheckboxTextToTheNodeValue() {
        subject.bind(leafNode)

        assertEquals(leafNode.getValue().str, subject.itemView.checkText.text)
    }

    @Test
    fun bind_setIndentationByLevelOfDepth() {
        subject.bind(leafNode)

        assertEquals(10, subject.itemView.indentation.minimumWidth)
    }
}