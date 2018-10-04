package com.f3401pal.checkabletreeview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_checkable_text.view.*

private const val TAG = "SingleRecyclerView"

class SingleRecyclerViewImpl<T : Checkable> : RecyclerView, CheckableTreeView<T> {

    private val adapter: TreeAdapter<T> by lazy {
        val indentation = indentation.px
        TreeAdapter<T>(indentation)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }

    @UiThread
    override fun setRoots(roots: List<TreeNode<T>>) {
        with(adapter) {
            nodes.clear()
            nodes.addAll(roots)
            // TODO: restore expanded state
            expandedNodeIds.clear()

            notifyDataSetChanged()
        }
    }

}

class TreeAdapter<T : Checkable>(private val indentation: Int) : RecyclerView.Adapter<TreeAdapter<T>.ViewHolder>() {

    internal val nodes: MutableList<TreeNode<T>> = mutableListOf()
    internal val expandedNodeIds = mutableSetOf<Long>()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return nodes[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_checkable_text, parent, false), indentation)
    }

    override fun getItemCount(): Int {
        return nodes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(nodes[position])
    }

    @UiThread
    private fun expand(position: Int) {
        if(position >= 0) {
            // expand
            val node = nodes[position]
            val insertPosition = position + 1
            nodes.addAll(insertPosition, node.getChildren())
            expandedNodeIds.add(node.id)
            notifyDataSetChanged()
        }
    }

    @UiThread
    private fun collapse(position: Int) {
        // collapse
        if(position >= 0) {
            val node = nodes[position]
            fun removeChildrenFrom(cur: TreeNode<T>) {
                nodes.remove(cur)
                if(expandedNodeIds.contains(cur.id)) {
                    expandedNodeIds.remove(cur.id)
                    cur.getChildren().forEach { removeChildrenFrom(it) }
                }
            }
            node.getChildren().forEach { removeChildrenFrom(it) }

            expandedNodeIds.remove(node.id)
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(view: View, private val indentation: Int) : RecyclerView.ViewHolder(view) {

        internal fun bind(node: TreeNode<T>) {
            itemView.indentation.minimumWidth = indentation * node.getLevel()

            itemView.checkText.text = node.getValue().toString()
            itemView.checkText.setOnCheckedChangeListener(null)
            val state = node.getCheckedStatus()
            itemView.checkText.isChecked = state.allChildrenChecked
            itemView.checkText.setIndeterminate(state.hasChildChecked)
            itemView.checkText.setOnCheckedChangeListener { _, isChecked ->
                node.setChecked(isChecked)
                notifyDataSetChanged()
            }

            if(node.isLeaf()) {
                itemView.expandIndicator.visibility = View.GONE
            } else {
                itemView.expandIndicator.visibility = View.VISIBLE
                itemView.expandIndicator.setImageResource(if(expandedNodeIds.contains(node.id)) R.drawable.ic_remove_black_24dp else R.drawable.ic_add_black_24dp)
                if(expandedNodeIds.contains(node.id)) {
                    itemView.expandIndicator.setOnClickListener { collapse(adapterPosition) }
                } else {
                    itemView.expandIndicator.setOnClickListener { expand(adapterPosition) }
                }
            }

            Log.d(TAG, "${node.getValue()}: hasChildChecked=${state.hasChildChecked}, allChildrenChecked=${state.allChildrenChecked}")
        }

    }
}

