package com.example.soccertshirts_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.databinding.ItemJerseyBinding

class JerseyAdapter(private var jerseys: List<Jersey>) :
    RecyclerView.Adapter<JerseyAdapter.JerseyViewHolder>() {

    class JerseyViewHolder(val binding: ItemJerseyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JerseyViewHolder {
        val binding = ItemJerseyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JerseyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JerseyViewHolder, position: Int) {
        val jersey = jerseys[position]
        holder.binding.apply {
            tvTitle.text = jersey.title
            tvTeam.text = jersey.team
            tvPrice.text = "$${jersey.price}"
        }
    }

    override fun getItemCount(): Int = jerseys.size

    fun updateData(newJerseys: List<Jersey>) {
        jerseys = newJerseys
        notifyDataSetChanged()
    }
}