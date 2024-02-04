package com.vbevia.montanejosqr.registerevent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.dbuuidevents.EventsUuidDBModel

class RegisterEventAdapter(var dataList: List<EventsUuidDBModel>, private val onItemClick: (EventsUuidDBModel, Int) -> Unit) :
    RecyclerView.Adapter<RegisterEventAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.titleTextView)
        var uuid: TextView = itemView.findViewById(R.id.contentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the item layout XML and create a new ViewHolder instance
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.eventos_item_layout, parent, false)
        return MyViewHolder(itemView) // Pass any necessary parameters to your ViewHolder constructor
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.title.text = dataList[position].event
        holder.uuid.text = dataList[position].uuid

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(item, position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
