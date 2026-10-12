package com.example.midterm_ai.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.midterm_ai.R
import com.example.midterm_ai.data.local.EventEntity

class EventAdapter(
    private var events: List<EventEntity> = emptyList(),
    private val onDeleteEvent: (EventEntity) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    fun submitList(newList: List<EventEntity>) {
        events = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_event_title)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tv_event_datetime)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_event)

        fun bind(event: EventEntity) {
            tvTitle.text = event.title
            tvDateTime.text = "${event.date} - ${event.time}"
            btnDelete.setOnClickListener {
                onDeleteEvent(event)
            }
        }
    }
}
