package com.example.soccertshirts_app.adapters

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soccertshirts_app.R
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.databinding.ItemJerseyBinding
import com.squareup.picasso.Picasso

class JerseyAdapter(
    private var jerseys: List<Jersey>,
    private val currentUserId: String?,
    private val onItemClick: (Jersey) -> Unit,
    private val onEditClick: (Jersey) -> Unit,
    private val onDeleteClick: (Jersey) -> Unit,
    private val onLikeClick: (Jersey) -> Unit,
    private val onCommentClick: (Jersey) -> Unit
) : RecyclerView.Adapter<JerseyAdapter.JerseyViewHolder>() {

    class JerseyViewHolder(val binding: ItemJerseyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JerseyViewHolder {
        val binding = ItemJerseyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JerseyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JerseyViewHolder, position: Int) {
        val jersey = jerseys[position]
        holder.binding.apply {
            root.setOnClickListener { onItemClick(jersey) }
            
            tvTitle.text = jersey.title
            tvTeam.text = jersey.team
            tvCountry.text = if (jersey.country.isNotEmpty()) "(${jersey.country})" else ""
            tvPrice.text = "$${jersey.price}"
            tvOwnerName.text = jersey.ownerName.ifEmpty { "Anonymous" }
            tvLikesCount.text = jersey.likesCount.toString()
            tvCommentsCount.text = jersey.commentsCount.toString()

            // Like status - Heart icon
            val isLiked = currentUserId != null && jersey.likedBy.contains(currentUserId)
            ibLike.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled
                else R.drawable.ic_heart_outline
            )
            ibLike.setOnClickListener { onLikeClick(jersey) }

            // Comment button
            ibComment.setOnClickListener { onCommentClick(jersey) }

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

            // Comments Preview Section
            if (jersey.recentComments.isNotEmpty()) {
                llCommentsPreview.visibility = View.VISIBLE
                llCommentsContainer.removeAllViews()
                
                // Show up to 3 latest comments
                jersey.recentComments.take(3).forEach { comment ->
                    val textView = TextView(root.context).apply {
                        val spannable = SpannableStringBuilder()
                        spannable.append(comment.username, StyleSpan(Typeface.BOLD), 0)
                        spannable.append(": ${comment.text}")
                        text = spannable
                        textSize = 13f
                        setPadding(0, 2, 0, 4)
                    }
                    llCommentsContainer.addView(textView)
                }

                if (jersey.commentsCount > 3) {
                    tvViewAllComments.visibility = View.VISIBLE
                    tvViewAllComments.text = "View all ${jersey.commentsCount} comments"
                    tvViewAllComments.setOnClickListener { onCommentClick(jersey) }
                } else {
                    tvViewAllComments.visibility = View.GONE
                }
            } else {
                llCommentsPreview.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = jerseys.size

    fun updateData(newJerseys: List<Jersey>) {
        jerseys = newJerseys
        notifyDataSetChanged()
    }
}