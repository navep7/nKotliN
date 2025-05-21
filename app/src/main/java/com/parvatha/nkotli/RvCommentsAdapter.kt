package com.parvatha.nkotli

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RvCommentsAdapter internal constructor(
    context: Context?,
    private val mComments: List<Comment>
) :
    RecyclerView.Adapter<RvCommentsAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.rv_comments_item, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment: Comment = mComments[position]
        holder.txComment.text = Html.fromHtml(comment.strComment)
        holder.txTime.text = comment.strCommentTime
        holder.txUser.text = comment.strCommentUser
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mComments.size
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var txComment: TextView = itemView.findViewById(R.id.tx_comment)
        var txTime: TextView = itemView.findViewById(R.id.tx_time)
        var txUser: TextView = itemView.findViewById(R.id.tx_user)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return mComments[0].toString()
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}
