package com.example.soccertshirts_app.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soccertshirts_app.data.model.Comment
import com.example.soccertshirts_app.databinding.ItemCommentBinding
import com.squareup.picasso.Picasso

class CommentAdapter(private var comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.apply {
            tvCommentUsername.text = comment.username
            tvCommentText.text = comment.text
            
            // Relative time (e.g., "2 hours ago")
            tvCommentDate.text = DateUtils.getRelativeTimeSpanString(
                comment.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            if (comment.userProfileImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(comment.userProfileImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivCommentUserProfile)
            } else {
                ivCommentUserProfile.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateData(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}