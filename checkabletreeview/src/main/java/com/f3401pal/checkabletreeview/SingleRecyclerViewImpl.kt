package com.f3401pal.checkabletreeview

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
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

            notifyDataSetChanged()
        }
    }

}

class TreeAdapter<T : Checkable>(private val indentation: Int) : RecyclerView.Adapter<TreeAdapter<T>.ViewHolder>() {

    internal val nodes: MutableList<TreeNode<T>> = mutableListOf()

    private val expandCollapseToggleHandler: (TreeNode<T>, ViewHolder) -> Unit = { node, viewHolder ->
        if(node.isExpanded) {
            collapse(viewHolder.adapterPosition)
        } else {
            expand(viewHolder.adapterPosition)
        }
        startAnimation(node.isExpanded, viewHolder.itemView.expandIndicator)
    }

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
            val insertedSize = node.getChildren().size
            nodes.addAll(insertPosition, node.getChildren())
            node.isExpanded = true
            notifyItemRangeInserted(insertPosition, insertedSize)
        }
    }

    @UiThread
    private fun collapse(position: Int) {
        // collapse
        if(position >= 0) {
            val node = nodes[position]
            var removeCount = 0
            fun removeChildrenFrom(cur: TreeNode<T>) {
                nodes.remove(cur)
                removeCount++
                if(cur.isExpanded) {
                    cur.getChildren().forEach { removeChildrenFrom(it) }
                    node.isExpanded = false
                }
            }
            node.getChildren().forEach { removeChildrenFrom(it) }
            node.isExpanded = false
            notifyItemRangeRemoved(position + 1, removeCount)
        }
    }

    @UiThread
    private fun startAnimation(expand: Boolean, imageView: ImageView) {
        imageView.setImageResource(if(expand) R.drawable.ic_expand_toggle_animated else R.drawable.ic_collapse_toggle_animated)
        (imageView.drawable as Animatable).start()
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
                itemView.expandIndicator.setOnClickListener { expandCollapseToggleHandler(node, this) }
            }

            Log.d(TAG, "${node.getValue()}: hasChildChecked=${state.hasChildChecked}, allChildrenChecked=${state.allChildrenChecked}")
        }

    }
}

