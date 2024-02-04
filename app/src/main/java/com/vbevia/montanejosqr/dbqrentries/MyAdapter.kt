package com.vbevia.montanejosqr.dbqrentries

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vbevia.montanejosqr.R

class MyAdapter(var dataList: List<MainDBModel>, private val context: Context) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var uuid: TextView = itemView.findViewById(R.id.uuid)
        var time: TextView = itemView.findViewById(R.id.time)
        var concept: TextView = itemView.findViewById(R.id.concept)
        var eventUuid: TextView = itemView.findViewById(R.id.eventUuid)
        var state: TextView = itemView.findViewById(R.id.state)
        var availableUsages: TextView = itemView.findViewById(R.id.availableUsages)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.uuid.text = dataList[position].uuid
        holder.time.text = dataList[position].time
        holder.concept.text = dataList[position].concept
        holder.eventUuid.text = dataList[position].eventUuid
        holder.state.text = dataList[position].state
        holder.availableUsages.text = dataList[position].availableUsages
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
