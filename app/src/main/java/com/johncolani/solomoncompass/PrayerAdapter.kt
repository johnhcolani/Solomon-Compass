package com.johncolani.solomoncompass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrayerAdapter(private val prayers: List<String>) :
    RecyclerView.Adapter<PrayerAdapter.PrayerViewHolder>() {

    class PrayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prayerText: TextView = itemView.findViewById(R.id.prayer_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prayer, parent, false)
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        holder.prayerText.text = prayers[position]
    }

    override fun getItemCount(): Int = prayers.size
}