package com.vbevia.montanejosqr.registerevent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.registerevent.models.RegisterEventModel

class ItemAdapter(private val items: List<RegisterEventModel>, private val clickListener: OnItemClickListener) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: RegisterEventModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.register_device_list, parent, false)
        return ItemViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: View, private val clickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val uuidTextView: TextView = itemView.findViewById(R.id.textViewUuid)
        // Initialize other TextViews here

        fun bind(item: RegisterEventModel) {
            nameTextView.text = item.name
            uuidTextView.text = item.uuid

            itemView.setOnClickListener {
                clickListener.onItemClick(item)
            }
        }
    }
}
