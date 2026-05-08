package com.example.soccertshirts_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.databinding.ItemJerseyBinding
import com.squareup.picasso.Picasso

class JerseyAdapter(
    private var jerseys: List<Jersey>,
    private val currentUserId: String?,
    private val onEditClick: (Jersey) -> Unit,
    private val onDeleteClick: (Jersey) -> Unit
) : RecyclerView.Adapter<JerseyAdapter.JerseyViewHolder>() {

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
            tvOwnerName.text = jersey.ownerName.ifEmpty { "Anonymous" }

            // Load Jersey Image
            if (jersey.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(jersey.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivJerseyImage)
            } else {
                ivJerseyImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Load Owner Profile Image
            if (jersey.ownerProfileImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(jersey.ownerProfileImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivOwnerProfile)
            } else {
                ivOwnerProfile.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Show actions only for the owner
            if (currentUserId != null && jersey.ownerId == currentUserId) {
                llActions.visibility = View.VISIBLE
                ibEdit.setOnClickListener { onEditClick(jersey) }
                ibDelete.setOnClickListener { onDeleteClick(jersey) }
            } else {
                llActions.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = jerseys.size

    fun updateData(newJerseys: List<Jersey>) {
        jerseys = newJerseys
        notifyDataSetChanged()
    }
}