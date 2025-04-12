package com.parvatha.nkotli

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList


class MyRecyclerViewAdapter internal constructor(
    context: Context?,
    private var mData: List<String>
) :
    RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>(), Filterable {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null


    private val searchFilter : Filter = object : Filter() {
        override fun performFiltering(input: CharSequence): FilterResults {
            val filteredList = if (input.isEmpty()) {
                mData
            } else {
                mData.filter { it.lowercase().contains(input) }
            }
            return FilterResults().apply { values = filteredList }
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(input: CharSequence, results: FilterResults) {
            IndexActivity.arrayListIndex.clear()
            IndexActivity.arrayListIndex.addAll(results.values as ArrayList<String>) //= results.values as ArrayList<String>
            IndexActivity.rvAdapter.notifyDataSetChanged()
        }
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.rv_item, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = mData[position]
        holder.myTextView.text = "${position + 1}.  $title"
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var myTextView: TextView = itemView.findViewById(R.id.tv_index)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return mData[id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: IndexActivity) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    override fun getFilter(): Filter {
        return searchFilter
    }
}
