package com.hzn.easypulllayout

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * RecyclerView adapter
 *
 * Created by huzenan on 2017/8/12.
 */
class RvAdapter(var context: Context, var list: List<Any>) : RecyclerView.Adapter<RvAdapter.ViewHolderString>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolderString {
        return ViewHolderString(LayoutInflater.from(context).inflate(R.layout.item_string, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderString?, position: Int) {
        holder?.tv?.text = list[position] as String
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolderString(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv: TextView? = null

        init {
            tv = itemView.findViewById(R.id.tv) as TextView?
        }
    }
}